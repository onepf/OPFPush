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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PatternMatcher;
import android.text.TextUtils;
import android.util.Log;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.util.PackageUtils;

import java.security.Provider;
import java.util.List;

/**
 * Helper class for manage push providers.
 * For get instance call {@link OpenPushHelper#getInstance(android.content.Context)}.\
 *
 * @author Kirill Rozov
 * @since 04.09.2014
 */
public class OpenPushHelper {

    private static final String TAG = "OpenPushHelper";

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

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

    private Options mOptions;

    @Nullable
    private PushProvider mCurrentProvider;

    @Nullable
    private RetryRegistrationRunnable mRegistrationRunnable;

    private State mState = State.STATE_NONE;
    private int mRetryNumber = 0;

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

        PushProvider provider = getLastProvider();
        if (provider != null) {
            if (provider.isAvailable()) {
                if (provider.isRegistered()) {
                    mCurrentProvider = provider;
                    mState = State.STATE_RUNNING;
                } else if (!registerProvider(provider)) {
                    saveLastProvider(null);
                }
            } else {
                reset();
                mCurrentProvider = null;
                if (mListener != null) {
                    mListener.onProviderBecameUnavailable(provider.getName());
                }

                if (options.isRecoverProvider()) {
                    register();
                }
            }
        }
    }

    public void setListener(@Nullable OpenPushListener l) {
        mListener = l;
    }

    public synchronized void register() {
        checkInitDone();

        switch (mState) {
            case STATE_NONE:
            case STATE_NO_AVAILABLE_PROVIDERS:
                mState = State.STATE_REGISTRATION_RUNNING;
                registerNextProvider(null);
                break;

            case STATE_UNREGISTRATION_RUNNING:
                throw new OpenPushException("Can't register while unregistration is running.");

            default:
                throw new OpenPushException("Attempt to register twice!");
        }
    }

    private void registerNextProvider(@Nullable PushProvider provider) {
        int i = 0;
        final List<PushProvider> providers = mOptions.getProviders();
        if (provider != null) {
            int lastCandidateIndex = providers.indexOf(provider);
            if (lastCandidateIndex != -1) {
                i = lastCandidateIndex + 1;
            }
        }

        for (int cnt = providers.size(); i < cnt; ++i) {
            if (registerProvider(providers.get(i))) {
                return;
            }
        }

        mState = State.STATE_NO_AVAILABLE_PROVIDERS;
        if (mListener != null) {
            mListener.onNoAvailableProvider();
        }
    }

    /**
     * Start registerProvider provider.
     *
     * @param provider Provider for registration.
     * @return Did registration start.
     */
    private boolean registerProvider(@NotNull PushProvider provider) {
        if (provider.isAvailable()) {
            mState = State.STATE_REGISTRATION_RUNNING;
            provider.register();
            return true;
        } else {
            return false;
        }
    }

    public void unregister() {
        checkInitDone();

        if (mCurrentProvider != null && mState == State.STATE_RUNNING) {
            mState = State.STATE_UNREGISTRATION_RUNNING;
            unregisterPackageChangeReceiver();
            mCurrentProvider.unregister();
        } else {
            throw new IllegalStateException("Attempt to unregister not initialised!");
        }
    }

    private void registerPackageChangeReceiver(@NotNull PushProvider provider) {
        mPackageReceiver = new PackageChangeReceiver(provider);

        IntentFilter appUpdateFilter = new IntentFilter(Intent.ACTION_PACKAGE_REPLACED);
        appUpdateFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
        appUpdateFilter.addDataPath(mAppContext.getPackageName(), PatternMatcher.PATTERN_LITERAL);
        mAppContext.registerReceiver(mPackageReceiver, appUpdateFilter);

        try {
            // System apps can't be removed, that's why no sense listen package remove event.
            if (PackageUtils.isSystemApp(mAppContext, provider.getHostAppPackage())) {
                IntentFilter hostAppRemovedFilter
                        = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
                hostAppRemovedFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
                hostAppRemovedFilter.addDataPath(
                        provider.getHostAppPackage(), PatternMatcher.PATTERN_LITERAL);
                mAppContext.registerReceiver(mPackageReceiver, hostAppRemovedFilter);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, String.format("Can not find package '%s'.",
                    provider.getHostAppPackage()), e);
        }
    }

    private void unregisterPackageChangeReceiver() {
        if (mPackageReceiver != null) {
            mAppContext.unregisterReceiver(mPackageReceiver);
            mPackageReceiver = null;
        }
    }

    @Nullable
    public String getCurrentProviderName() {
        return mCurrentProvider == null ? null : mCurrentProvider.getName();
    }

    @Nullable
    public String getCurrentProviderRegistrationId() {
        return mCurrentProvider == null ? null : mCurrentProvider.getRegistrationId();
    }

    @Nullable
    private PushProvider getProviderByName(@NotNull String providerName) {
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
                PushProvider provider = getProviderByName(storedProviderName);
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

    public State getState() {
        return mState;
    }

    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        if (mListener != null) {
            mListener.onMessage(providerName, extras);
        }
    }

    public void onDeletedMessages(@NotNull String providerName, int messagesCount) {
        if (mListener != null) {
            mListener.onDeletedMessages(providerName, messagesCount);
        }
    }

    public void onNeedRetryRegister(@NotNull String providerName) {
        if (mCurrentProvider != null && mCurrentProvider.getName().equals(providerName)) {
            reset();
            mCurrentProvider.onAppStateChanged();
            registerProvider(mCurrentProvider);
        }
    }

    private void reset() {
        mPreferences.edit().clear().apply();
        mState = State.STATE_NONE;
        cancelRetryRegistration();
    }

    public void onProviderBecameUnavailable(@NotNull PushProvider provider) {
        if (mCurrentProvider != null && mCurrentProvider.equals(provider)) {
            reset();
            mCurrentProvider = null;
            if (mListener != null) {
                mListener.onProviderBecameUnavailable(provider.getName());
            }

            if (mOptions.isRecoverProvider()) {
                register(); //Restart registration
            }
        }
    }

    public void onUnregistrationEnd(@NotNull RegistrationResult result) {
        if (mState != State.STATE_UNREGISTRATION_RUNNING) {
            return;
        }

        if (result.isSuccess()) {
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
            mState = State.STATE_RUNNING;
            final PushProvider provider = getProviderByName(result.getProviderName());
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

    public void onRegistrationEnd(@NotNull RegistrationResult result) {
        if (mState != State.STATE_REGISTRATION_RUNNING) {
            return;
        }

        if (result.isSuccess()) {
            mState = State.STATE_RUNNING;
            cancelRetryRegistration();

            mCurrentProvider = getProviderByName(result.getProviderName());
            saveLastProvider(mCurrentProvider);
            Assert.assertNotNull(result.getRegistrationId());
            if (mListener != null) {
                mListener.onRegistered(result.getProviderName(), result.getRegistrationId());
            }

            Assert.assertNotNull(mCurrentProvider);
            registerPackageChangeReceiver(mCurrentProvider);
        } else {
            PushProvider provider = getProviderByName(result.getProviderName());
            Assert.assertNotNull(provider);

            if (!result.isRecoverableError() || !postRegistrationRetry(provider)) {
                if (mListener != null) {
                    mListener.onRegistrationError(provider.getName(), result.getErrorCode());
                }

                mRetryNumber = 0;
                registerNextProvider(provider);
            }
        }
    }

    private void cancelRetryRegistration() {
        mRetryNumber = 0;
        if (mRegistrationRunnable != null) {
            sHandler.removeCallbacks(mRegistrationRunnable);
            mRegistrationRunnable = null;
        }
    }

    private boolean postRegistrationRetry(@NotNull PushProvider provider) {
        if (mOptions.getBackoff() != null
                && mRetryNumber < mOptions.getBackoff().getTryCount()) {
            long start = System.currentTimeMillis() + mOptions.getBackoff().getDelay(mRetryNumber);
            mRetryNumber++;
            if (mRegistrationRunnable == null
                    || !mRegistrationRunnable.getProvider().equals(provider)) {
                mRegistrationRunnable = new RetryRegistrationRunnable(provider);
            }
            sHandler.postAtTime(mRegistrationRunnable, start);
            return true;
        } else {
            return false;
        }
    }

    public static enum State {
        /**
         * Push isn't running.
         */
        STATE_NONE,

        /**
         * Helper is selecting provider for registerProvider push.
         */
        STATE_REGISTRATION_RUNNING,

        /**
         * Helper select provider and successfully registerProvider it.
         */
        STATE_RUNNING,

        /**
         * All providers are unavailable or they registration failed.
         */
        STATE_NO_AVAILABLE_PROVIDERS,

        /**
         * Helper is unregistering current provider.
         */
        STATE_UNREGISTRATION_RUNNING
    }

    private static class RetryRegistrationRunnable implements Runnable {
        private final PushProvider mProvider;

        RetryRegistrationRunnable(@NotNull PushProvider provider) {
            mProvider = provider;
        }

        @NotNull
        public PushProvider getProvider() {
            return mProvider;
        }

        @Override
        public void run() {
            mProvider.register();
        }
    }
}
