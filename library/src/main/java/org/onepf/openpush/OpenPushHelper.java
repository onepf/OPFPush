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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.text.TextUtils;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.util.PackageUtils;

import java.util.List;

import static org.onepf.openpush.OpenPushLog.LOGD;
import static org.onepf.openpush.OpenPushLog.LOGI;
import static org.onepf.openpush.OpenPushLog.LOGW;

/**
 * Helper class for manage push providers.
 * For get instance call {@link OpenPushHelper#getInstance(android.content.Context)}.\
 *
 * @author Kirill Rozov
 * @since 04.09.2014
 */
public class OpenPushHelper {

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";

    @Nullable
    private OpenPushListener mListener;

    @NotNull
    private final Context mAppContext;

    @NotNull
    private final SharedPreferences mPreferences;

    @Nullable
    private static OpenPushHelper sInstance;

    @Nullable
    private PackageChangeReceiver mPackageReceiver;

    @Nullable
    private PushProvider mCurrentProvider;

    @NotNull
    private volatile State mState = State.NONE;

    private Options mOptions;

    public static OpenPushHelper getInstance(@NotNull Context context) {
        if (sInstance == null) {
            synchronized (OpenPushHelper.class) {
                if (sInstance == null) {
                    sInstance = new OpenPushHelper(context);
                }
            }
        }
        return sInstance;
    }

    private OpenPushHelper(@NotNull Context context) {
        mAppContext = context.getApplicationContext();
        mPreferences =
                mAppContext.getSharedPreferences("org.onepf.openpush", Context.MODE_PRIVATE);
    }

    public boolean isInitDone() {
        return mOptions != null;
    }

    public void init(@NotNull Options options) {
        if (isInitDone()) {
            throw new OpenPushException("Try to init OpenPushHelper twice.");
        }
        mOptions = options;
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
                mState = State.RUNNING;
            } else {
                LOGI("Last provider need register.");
                mState = State.REGISTRATION_RUNNING;
                if (!registerProvider(lastProvider)) {
                    mState = State.NONE;
                    saveLastProvider(null);
                }
            }
        } else {
            LOGI("Last provider isn't available.");
            reset();
            if (mOptions.isRecoverProvider()) {
                LOGI("Try register any available provider.");
                register();
            }
        }
    }

    public boolean isRegistered() {
        return mCurrentProvider != null && mCurrentProvider.isRegistered();
    }

    public void setListener(@Nullable OpenPushListener l) {
        mListener = l;
    }

    public void register() {
        checkInitDone();

        switch (mState) {
            case REGISTRATION_RUNNING:
                break;

            case NONE:
            case NO_AVAILABLE_PROVIDERS:
                mState = State.REGISTRATION_RUNNING;
                if (mOptions.isSystemPushPreferred()
                        && registerSystemPreferredProvider()) {
                    return;
                }
                registerNextProvider(null);
                break;

            case UNREGISTRATION_RUNNING:
                throw new OpenPushException("Can't register while unregistration is running.");

            default:
                throw new OpenPushException("Attempt to register twice!");
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

        mState = State.NO_AVAILABLE_PROVIDERS;
        LOGW("No more available providers.");
        if (mListener != null) {
            mListener.onNoAvailableProvider();
        }
        return false;
    }

    /**
     * Same that {@link #registerProvider(PushProvider, boolean)} with {@code registerNext} set to false.
     */
    private boolean registerProvider(@NotNull PushProvider provider) {
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
    private boolean registerProvider(@NotNull PushProvider provider, boolean tryRegisterNext) {
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

        if (mCurrentProvider == null) {
            throw new OpenPushException("No provider to unregister!");
        }

        switch (mState) {
            case UNREGISTRATION_RUNNING:
                break;

            case RUNNING:
                mState = State.UNREGISTRATION_RUNNING;
                unregisterPackageChangeReceiver();
                mCurrentProvider.unregister();
                break;

            case REGISTRATION_RUNNING:
                throw new OpenPushException("Can't unregister while registration is running.");

            default:
                throw new OpenPushException("Before to unregister you must register provider.!");
        }
    }

    private void registerPackageChangeReceiver(@NotNull PushProvider provider) {
        mPackageReceiver = new PackageChangeReceiver(provider);

        IntentFilter appUpdateFilter = new IntentFilter(Intent.ACTION_PACKAGE_REPLACED);
        appUpdateFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
        appUpdateFilter.addDataPath(mAppContext.getPackageName(), PatternMatcher.PATTERN_LITERAL);
        mAppContext.registerReceiver(mPackageReceiver, appUpdateFilter);

        // System apps can't be removed, that's why no sense listen package remove event.
        if (PackageUtils.isSystemApp(mAppContext, provider.getHostAppPackage())) {
            IntentFilter hostAppRemovedFilter
                    = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            hostAppRemovedFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
            hostAppRemovedFilter.addDataPath(
                    provider.getHostAppPackage(), PatternMatcher.PATTERN_LITERAL);
            mAppContext.registerReceiver(mPackageReceiver, hostAppRemovedFilter);
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

    @Nullable
    private PushProvider getProvider(@NotNull String providerName) {
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

    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        LOGD(String.format("onUnavailable(providerName = %s).", providerName));
        if (mListener != null) {
            mListener.onMessage(providerName, extras);
        }
    }

    public void onDeletedMessages(@NotNull String providerName, int messagesCount) {
        LOGD(String.format("onDeletedMessages(providerName = %s,messagesCount = %d).",
                providerName, messagesCount));
        if (mListener != null) {
            mListener.onDeletedMessages(providerName, messagesCount);
        }
    }

    public void onNeedRetryRegister(@NotNull String providerName) {
        LOGD(String.format("onNeedRetryRegister(providerName = %s).", providerName));
        if (mCurrentProvider != null && mCurrentProvider.getName().equals(providerName)) {
            reset();
            mCurrentProvider.onAppStateChanged();
            mState = State.REGISTRATION_RUNNING;
            if (!registerProvider(mCurrentProvider, false)) {
                mState = State.NONE;
            }
        }
    }

    private void reset() {
        mPreferences.edit().clear().apply();
        mState = State.NONE;
    }

    public void onUnavailable(@NotNull PushProvider provider) {
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

    private void onUnregistrationEnd(@NotNull RegistrationResult result) {
        if (mState != State.UNREGISTRATION_RUNNING) {
            return;
        }

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
            mState = State.RUNNING;
            final PushProvider provider = getProvider(result.getProviderName());
            if (provider != null) {
                mListener.onUnregistrationError(provider.getName(), result.getErrorCode());
            }
        }
    }

    private void checkInitDone() {
        if (!isInitDone()) {
            throw new OpenPushException("Before work with OpenPushHelper call init() first.");
        }
    }

    public void onResult(RegistrationResult result) {
        if (mState == State.REGISTRATION_RUNNING) {
            onRegistrationEnd(result);
        } else if (mState == State.UNREGISTRATION_RUNNING) {
            onUnregistrationEnd(result);
        } else {
            throw new UnsupportedOperationException("New result can be handled only when" +
                    " registration or unregistration is running.");
        }
    }

    private void onRegistrationEnd(@NotNull RegistrationResult result) {
        if (mState != State.REGISTRATION_RUNNING) {
            return;
        }

        if (result.isSuccess()) {
            LOGI(String.format("Successfully register provider '%s'.", result.getProviderName()));
            mState = State.RUNNING;

            mCurrentProvider = getProvider(result.getProviderName());
            saveLastProvider(mCurrentProvider);
            Assert.assertNotNull(result.getRegistrationId());
            if (mListener != null) {
                mListener.onRegistered(result.getProviderName(), result.getRegistrationId());
            }

            Assert.assertNotNull(mCurrentProvider);
            registerPackageChangeReceiver(mCurrentProvider);
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
                "state=" + mState +
                ", options=" + mOptions +
                ", currentProvider=" + mCurrentProvider +
                '}';
    }

    private static enum State {
        /**
         * Push isn't running.
         */
        NONE,

        /**
         * Helper is selecting provider for registerProvider push.
         */
        REGISTRATION_RUNNING,

        /**
         * Helper select provider and successfully registerProvider it.
         */
        RUNNING,

        /**
         * All providers are unavailable or they registration failed.
         */
        NO_AVAILABLE_PROVIDERS,

        /**
         * Helper is unregistering current provider.
         */
        UNREGISTRATION_RUNNING
    }
}
