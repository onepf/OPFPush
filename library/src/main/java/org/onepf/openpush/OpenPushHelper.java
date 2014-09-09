/*******************************************************************************
 * Copyright 2014 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

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
import android.util.Log;

import junit.framework.Assert;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.util.PackageUtils;

public class OpenPushHelper {

    private static final String TAG = "OpenPushHelper";

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_INIT_STATUS = "init_status";

    public static final int STATE_NONE = 0;
    public static final int STATE_REGISTRATION_RUNNING = 1;
    public static final int STATE_WORK = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_UNREGISTRATION_RUNNING = 4;

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

    @Nullable
    private Options mOptions;

    @Nullable
    private PushProvider mCurrentProvider;

    private int mState;
    private int mRetryNumber = 0;
    private RetryRegistrationRunnable mRegistrationRunnable;

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

    public void init(@NotNull Options options) {
        mOptions = options;

        PushProvider provider = getLastProvider();
        if (provider != null && provider.isAvailable()) {
            mCurrentProvider = provider;
            mState = mPreferences.getInt(KEY_INIT_STATUS, STATE_NONE);
        } else {
            mState = STATE_NONE;
        }

        if (mCurrentProvider != null && mState != STATE_WORK) {
            register(mCurrentProvider);
        }
    }

    public void setListener(@Nullable OpenPushListener l) {
        mListener = l;
    }

    public synchronized void register() {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        if (mState == STATE_NONE || mState == STATE_ERROR) {
            mState = STATE_REGISTRATION_RUNNING;

            PushProvider provider = getNextCandidate(null);
            if (provider == null || !register(provider)) {
                if (mListener != null) {
                    mListener.onNoAvailableProvider();
                }
                mState = STATE_NONE;
            }
        } else {
            throw new IllegalStateException("Attempt to register twice!");
        }
    }

    private boolean register(@NotNull PushProvider provider) {
        if (provider.isAvailable() && !provider.isRegistered()) {
            provider.register();
            return true;
        } else {
            return false;
        }
    }

    public void unregister() {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        if (mCurrentProvider != null && mState == STATE_WORK) {
            mState = STATE_UNREGISTRATION_RUNNING;
            unregisterPackageChangeReceiver();
            mCurrentProvider.unregister();
        } else {
            throw new IllegalStateException("Attempt to unregister not initialised!");
        }
    }

    @Nullable
    private PushProvider getNextCandidate(@Nullable PushProvider lastProvider) {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        int i = 0;
        if (lastProvider != null) {
            int lastCandidateIndex = mOptions.getProviders().indexOf(lastProvider);
            if (lastCandidateIndex != -1) {
                i = lastCandidateIndex + 1;
            }
        }
        for (int cnt = mOptions.getProviders().size(); i < cnt; ++i) {
            PushProvider candidate = mOptions.getProviders().get(i);
            if (candidate.isAvailable()) {
                return candidate;
            }
        }
        return null;
    }

    private void registerPackageChangeReceiver(@NotNull PushProvider provider) {
        try {
            // System apps can't be removed, that's why no sense listen package remove event.
            if (PackageUtils.isSystemApp(mAppContext, provider.getHostAppPackage())) {
                IntentFilter packageRemovedFilter
                        = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
                packageRemovedFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
                packageRemovedFilter.addDataPath(
                        provider.getHostAppPackage(), PatternMatcher.PATTERN_LITERAL);

                mPackageReceiver = new PackageChangeReceiver(provider);
                mAppContext.registerReceiver(mPackageReceiver, packageRemovedFilter);

                IntentFilter packageReplaceFilter =
                        new IntentFilter(Intent.ACTION_PACKAGE_REPLACED);
                packageReplaceFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
                packageReplaceFilter.addDataPath(
                        mAppContext.getPackageName(), PatternMatcher.PATTERN_LITERAL);
                mAppContext.registerReceiver(mPackageReceiver, packageReplaceFilter);
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
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        for (PushProvider provider : mOptions.getProviders()) {
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }

    @Nullable
    private PushProvider getLastProvider() {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        final String storedProviderName = mPreferences.getString(KEY_LAST_PROVIDER_NAME, null);
        if (storedProviderName != null) {
            for (PushProvider provider : mOptions.getProviders()) {
                if (storedProviderName.equals(provider.getName())) {
                    return provider;
                }
            }
        }
        return null;
    }

    private void saveProvider(@Nullable PushProvider provider) {
        if (provider == null) {
            mPreferences.edit()
                    .remove(KEY_LAST_PROVIDER_NAME)
                    .apply();
        } else {
            mPreferences.edit()
                    .putString(KEY_LAST_PROVIDER_NAME, provider.getName())
                    .apply();
        }
    }

    @MagicConstant(intValues = {
            STATE_ERROR,
            STATE_REGISTRATION_RUNNING,
            STATE_NONE,
            STATE_WORK,
            STATE_UNREGISTRATION_RUNNING
    })
    public int getState() {
        return mState;
    }

    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        if (mListener != null) {
            mListener.onMessage(providerName, extras);
        }
    }

    public void onDeletedMessages(@NotNull String providerName, @Nullable Bundle extras) {
        if (mListener != null) {
            mListener.onDeletedMessages(providerName, extras);
        }
    }

    public void onProviderNeedUpdate(@NotNull String providerName) {
        if (mCurrentProvider != null && providerName.equals(mCurrentProvider.getName())) {
            reset();
            mCurrentProvider.onAppStateChanged();
            register(mCurrentProvider);
        }
    }

    private void reset() {
        mPreferences.edit().clear().apply();
        mState = STATE_NONE;
        mRetryNumber = 0;
        if (mRegistrationRunnable != null) {
            sHandler.removeCallbacks(mRegistrationRunnable);
            mRegistrationRunnable = null;
        }
    }

    void onHostAppRemoved(@NotNull PushProvider provider) {
        if (mCurrentProvider != null && mCurrentProvider.equals(provider)) {
            reset();
            mCurrentProvider = null;
            if (mListener != null) {
                mListener.onHostAppRemoved(provider.getName(), provider.getHostAppPackage());
            }
            register(); //Restart registration
        }
    }

    public void onUnregistrationEnd(@NotNull RegistrationResult result) {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }
        if (mState != STATE_UNREGISTRATION_RUNNING) {
            return;
        }

        if (result.isSuccess()) {
            reset();
            mCurrentProvider = null;
            if (mListener != null) {
                mListener.onUnregistered(result.getProviderName(), result.getRegistrationId());
            }
        } else if (mListener != null) {
            mState = STATE_ERROR;
            final PushProvider provider = getProviderByName(result.getProviderName());
            Assert.assertNotNull(provider);
            mListener.onError(provider.getName(), result.getErrorCode());
        }
    }

    public void onRegistrationEnd(@NotNull RegistrationResult result) {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }
        if (mState != STATE_REGISTRATION_RUNNING) {
            return;
        }

        final PushProvider provider = getProviderByName(result.getProviderName());
        Assert.assertNotNull(provider);
        if (result.isSuccess()) {
            mState = STATE_WORK;
            mCurrentProvider = provider;
            mPreferences.edit().putInt(KEY_INIT_STATUS, STATE_WORK).apply();
            mRetryNumber = 0;
            saveProvider(mCurrentProvider);
            if (mListener != null) {
                mListener.onRegistered(result.getProviderName(), result.getRegistrationId());
            }

            registerPackageChangeReceiver(provider);
        } else {
            if (result.isRecoverableError() && mOptions.getBackoff() != null
                    && mRetryNumber < mOptions.getBackoff().tryCount()) {
                if (mRegistrationRunnable == null) {
                    mRegistrationRunnable = new RetryRegistrationRunnable(provider);
                }
                sHandler.postDelayed(mRegistrationRunnable,
                        mOptions.getBackoff().getDelay(mRetryNumber));
                mRetryNumber++;
            } else {
                if (mListener != null) {
                    mListener.onError(provider.getName(), result.getErrorCode());
                }

                mRetryNumber = 0;
                PushProvider nextProvider = getNextCandidate(provider);
                if (nextProvider != null) {
                    nextProvider.register();
                } else {
                    mState = STATE_ERROR;
                    mListener.onNoAvailableProvider();
                }
            }
        }
    }

    private static class RetryRegistrationRunnable implements Runnable {
        private final PushProvider mProvider;

        public RetryRegistrationRunnable(@NotNull PushProvider provider) {
            mProvider = provider;
        }

        @Override
        public void run() {
            mProvider.register();
        }
    }
}
