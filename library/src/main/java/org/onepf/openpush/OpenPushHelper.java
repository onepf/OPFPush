/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.openpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import junit.framework.Assert;

import org.onepf.openpush.util.PackageUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.onepf.openpush.OpenPushLog.LOGD;
import static org.onepf.openpush.OpenPushLog.LOGI;
import static org.onepf.openpush.OpenPushLog.LOGW;

/**
 * Helper class for manage push providers.
 * For get instance call {@link OpenPushHelper#getInstance(android.content.Context)}.\
 *
 * @author Kirill Rozov
 * @since 04.09.2014
 * <p/>
 * {@link }
 */
public class OpenPushHelper {

    static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    static final String PREF_NAME = "org.onepf.openpush";

    private static final int STATE_NONE = 0;
    private static final int STATE_REGISTERING = 1;
    private static final int STATE_WORKING = 2;
    private static final int STATE_UNREGISTERING = 3;

    @Nullable
    private static OpenPushHelper sInstance;

    @NonNull
    private final Context mAppContext;

    @NonNull
    private final SharedPreferences mPreferences;

    @Nullable
    private OpenPushListener mListener;

    @Nullable
    private BroadcastReceiver mPackageReceiver;

    @Nullable
    private PushProvider mCurrentProvider;

    private AtomicInteger mState = new AtomicInteger(STATE_NONE);

    private Options mOptions;

    private final Object mRegistrationLock = new Object();
    private final Object mInitLock = new Object();

    private OpenPushHelper(@NonNull Context context) {
        mAppContext = context.getApplicationContext();
        mPreferences =
                mAppContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static OpenPushHelper getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (OpenPushHelper.class) {
                if (sInstance == null) {
                    sInstance = new OpenPushHelper(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Create new instance of {@link OpenPushHelper}.
     * <b>Use for test purposes only!</b>
     *
     * @param context
     * @return New instance of {@link OpenPushHelper}.
     */
    static OpenPushHelper getNewInstance(@NonNull Context context) {
        synchronized (OpenPushHelper.class) {
            sInstance = new OpenPushHelper(context);
        }
        return sInstance;
    }

    public boolean isInitDone() {
        synchronized (mInitLock) {
            return mOptions != null;
        }
    }

    public boolean isRegistered() {
        return mCurrentProvider != null && mCurrentProvider.isRegistered();
    }

    private void checkInitDone() {
        if (!isInitDone()) {
            throw new OpenPushException("Before work with OpenPushHelper call init() first.");
        }
    }

    public void init(@NonNull Options options) {
        if (mOptions == null) {
            synchronized (mInitLock) {
                if (mOptions == null) {
                    mOptions = options;
                }
            }
        } else {
            throw new OpenPushException("Attempt to init twice.");
        }

        initLastProvider();
        LOGI("Init done.");
    }

    private void initLastProvider() {
        final PushProvider lastProvider = getLastProvider();
        if (lastProvider == null) {
            return;
        }

        LOGI(String.format("Try restore last provider '%s'.", lastProvider));

        if (lastProvider.isAvailable()) {
            if (lastProvider.isRegistered()) {
                LOGI("Last provider running.");
                mCurrentProvider = lastProvider;
                mState.set(STATE_WORKING);
            } else {
                LOGI("Last provider need register.");
                mState.set(STATE_REGISTERING);
                if (!registerProvider(lastProvider)) {
                    mState.set(STATE_NONE);
                    saveLastProvider(null);
                }
            }
        } else {
            lastProvider.onUnavailable();
            LOGI("Last provider isn't available.");
            reset();
            if (mOptions.isRecoverProvider()) {
                LOGI("Try register any available provider.");
                register();
            }
        }
    }

    public void setListener(@Nullable OpenPushListener l) {
        mListener = l == null ? null : new MainThreadListenerWrapper(l);
    }

    public void register() {
        checkInitDone();

        synchronized (mRegistrationLock) {

            switch (mState.get()) {
                case STATE_REGISTERING:
                    break;

                case STATE_NONE:
                    mState.set(STATE_REGISTERING);
                    if (mOptions.isSystemPushPreferred()
                            && registerSystemPreferredProvider()) {
                        return;
                    }
                    registerNextProvider(null);
                    break;

                case STATE_UNREGISTERING:
                    throw new OpenPushException("Can't register while unregistration is running.");

                case STATE_WORKING:
                    throw new OpenPushException("Attempt to register twice!");
            }
        }
    }

    private boolean registerSystemPreferredProvider() {
        for (PushProvider provider : mOptions.getProviders()) {
            if (PackageUtils.isSystemApp(mAppContext, provider.getHostAppPackage())
                    && registerProvider(provider)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Register first available provider. Iterate all provider from the next provider after
     * {@code lastProvider} param.
     *
     * @param lastProvider Last provider what check to register or null if has no.
     * @return True if find provider that can try to register, otherwise false.
     */
    private boolean registerNextProvider(@Nullable PushProvider lastProvider) {
        int nextProviderIndex = 0;
        final List<PushProvider> providers = mOptions.getProviders();
        if (lastProvider != null) {
            int lastProviderIndex = providers.indexOf(lastProvider);
            if (lastProviderIndex != -1) {
                nextProviderIndex = lastProviderIndex + 1;
            }
        }

        for (int providersCount = providers.size();
             nextProviderIndex < providersCount; ++nextProviderIndex) {

            if (registerProvider(providers.get(nextProviderIndex))) {
                return true;
            }
        }

        mState.set(STATE_NONE);
        LOGW("No more available providers.");
        if (mListener != null) {
            mListener.onNoAvailableProvider();
        }
        return false;
    }

    /**
     * Same that {@link #registerProvider(PushProvider, boolean)} with {@code registerNext} set to false.
     */
    private boolean registerProvider(@NonNull PushProvider provider) {
        return registerProvider(provider, false);
    }

    /**
     * Start register provider.
     *
     * @param provider        Provider for registration.
     * @param tryRegisterNext Try to register next available push provider after the {@code provider},
     *                        if the {@code provider} isn't available.
     * @return If provider available and can start registration return true, otherwise - false.
     */
    private boolean registerProvider(@NonNull PushProvider provider, boolean tryRegisterNext) {
        if (provider.isAvailable()) {
            LOGD(String.format("Try register %s.", provider));
            provider.register();
            return true;
        }
        LOGI(String.format("Provider '%s' not available.", provider));
        return tryRegisterNext && registerNextProvider(provider);

    }

    public void unregister() {
        checkInitDone();

        synchronized (mRegistrationLock) {
            if (!isRegistered()) {
                throw new OpenPushException("No one provider is registered!");
            }

            Assert.assertNotNull(mCurrentProvider);

            switch (mState.get()) {
                case STATE_WORKING:
                    mState.set(STATE_UNREGISTERING);
                    unregisterPackageChangeReceiver();
                    mCurrentProvider.unregister();
                    break;

                case STATE_UNREGISTERING:
                    break;

                case STATE_REGISTERING:
                    throw new OpenPushException("Can't unregister when registration in progress.!");

                case STATE_NONE:
                    throw new OpenPushException("Before to unregister you must register provider.!");
            }
        }
    }

    private void unregisterPackageChangeReceiver() {
        if (mPackageReceiver != null) {
            mAppContext.unregisterReceiver(mPackageReceiver);
            mPackageReceiver = null;
        }
    }

    @Nullable
    public PushProvider getCurrentProvider() {
        return mCurrentProvider;
    }

    /**
     * Search provider by name in {@code options} and return in.
     * If {@code} doesn't contain provider with described name return null.
     *
     * @param providerName Name of provider for search.
     * @return Provider with described name or null if nothing have found.
     */
    @Nullable
    private PushProvider getProvider(@NonNull String providerName) {
        for (PushProvider provider : mOptions.getProviders()) {
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }

    @Nullable
    private PushProvider getLastProvider() {
        if (mPreferences.contains(KEY_LAST_PROVIDER_NAME)) {
            String storedProviderName = mPreferences.getString(KEY_LAST_PROVIDER_NAME, null);
            if (!TextUtils.isEmpty(storedProviderName)) {
                PushProvider provider = getProvider(storedProviderName);
                if (provider != null) {
                    return provider;
                }
            }
            mPreferences.edit().remove(KEY_LAST_PROVIDER_NAME).apply();
        }
        return null;
    }

    private void saveLastProvider(@Nullable PushProvider provider) {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (provider == null) {
            editor.remove(KEY_LAST_PROVIDER_NAME);
        } else {
            editor.putString(KEY_LAST_PROVIDER_NAME, provider.getName());
        }
        editor.apply();
    }

    public void onMessage(@NonNull String providerName, @Nullable Bundle extras) {
        LOGD(String.format("onUnavailable(providerName = %s).", providerName));
        if (mListener != null) {
            mListener.onMessage(providerName, extras);
        }
    }

    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {
        LOGD(String.format("onDeletedMessages(providerName = %s,messagesCount = %d).",
                providerName, messagesCount));
        if (mListener != null) {
            mListener.onDeletedMessages(providerName, messagesCount);
        }
    }

    public void onNeedRetryRegister(@NonNull String providerName) {
        LOGD(String.format("onNeedRetryRegister(providerName = %s).", providerName));
        if (mCurrentProvider != null && mCurrentProvider.getName().equals(providerName)) {
            reset();
            mCurrentProvider.onAppStateChanged();
            mState.set(STATE_REGISTERING);
            if (!registerProvider(mCurrentProvider, false)) {
                mState.set(STATE_NONE);
            }
        }
    }

    void reset() {
        mPreferences.edit().clear().apply();
        mState.set(STATE_NONE);
    }

    public void onUnavailable(@NonNull PushProvider provider) {
        LOGD(String.format("onUnavailable(provider = %s).", provider));
        if (mCurrentProvider != null && mCurrentProvider.equals(provider)) {
            reset();
            mCurrentProvider.onUnavailable();
            mCurrentProvider = null;
            if (mListener != null) {
                mListener.onProviderBecameUnavailable(provider.getName());
            }

            if (mOptions.isRecoverProvider()) {
                register(); //Restart registration
            }
        }
    }

    public void onResult(Result result) {
        synchronized (mRegistrationLock) {
            switch (mState.get()) {
                case STATE_REGISTERING:
                    onRegistrationResult(result);
                    break;

                case STATE_UNREGISTERING:
                    onUnregistrationResult(result);
                    break;

                default:
                    throw new UnsupportedOperationException("New result can be handled only when" +
                            " registration or unregistration is running.");
            }
        }
    }

    private void onUnregistrationResult(@NonNull Result result) {
        if (result.isSuccess()) {
            LOGI(String.format("Successfully unregister provider '%s'.", result.getProviderName()));
            reset();
            if (mCurrentProvider != null) {
                mCurrentProvider.close();
                mCurrentProvider = null;
            }
            if (mListener != null) {
                Assert.assertNotNull(result.getRegistrationId());
                mListener.onUnregistered(result.getProviderName(), result.getRegistrationId());
            }
        } else if (mListener != null) {
            LOGI(String.format("Error unregister provider '%s'.", result.getProviderName()));
            final PushProvider provider = getProvider(result.getProviderName());
            if (provider != null) {
                mListener.onUnregistrationError(provider.getName(), result.getErrorCode());
            }
        }
    }

    private void onRegistrationResult(@NonNull Result result) {
        if (result.isSuccess()) {
            LOGI(String.format("Successfully register provider '%s'.", result.getProviderName()));
            LOGI(String.format("Register id '%s'.", result.getRegistrationId()));
            mState.set(STATE_WORKING);

            mCurrentProvider = getProvider(result.getProviderName());
            Assert.assertNotNull(mCurrentProvider);

            saveLastProvider(mCurrentProvider);
            Assert.assertNotNull(result.getRegistrationId());
            if (mListener != null) {
                mListener.onRegistered(result.getProviderName(), result.getRegistrationId());
            }

            mPackageReceiver =
                    PackageUtils.registerPackageChangeReceiver(mAppContext, mCurrentProvider);
        } else {
            LOGI(String.format("Error register provider '%s'.", result.getProviderName()));
            PushProvider provider = getProvider(result.getProviderName());
            if (provider != null) {
                if (mListener != null) {
                    mListener.onRegistrationError(provider.getName(), result.getErrorCode());
                }

                if (!result.isRecoverableError()) {
                    registerNextProvider(provider);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "OpenPushHelper{" +
                "options=" + mOptions +
                ", currentProvider=" + mCurrentProvider +
                ", inited=" + isInitDone() +
                ", registered=" + isRegistered() +
                '}';
    }

}
