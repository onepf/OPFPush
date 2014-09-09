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

    public static final int INIT_NOT_STARTED = 0;
    public static final int INIT_IN_PROGRESS = 1;
    public static final int INIT_SUCCESS = 2;
    public static final int INIT_ERROR = 3;

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

    private int mInitStatus;
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
                mAppContext.getSharedPreferences("org.onepf.openpush.prefs", Context.MODE_PRIVATE);
    }

    public void init(@NotNull Options options) {
        mOptions = options;

        PushProvider provider = getLastProvider();
        if (provider != null && provider.isAvailable()) {
            mCurrentProvider = provider;
            mInitStatus = mPreferences.getInt(KEY_INIT_STATUS, INIT_NOT_STARTED);
        } else {
            mInitStatus = INIT_NOT_STARTED;
        }

        if (mCurrentProvider != null && !mCurrentProvider.isRegistered()) {
            mInitStatus = INIT_IN_PROGRESS;
            mCurrentProvider.register();
        }
    }

    public void setListener(@Nullable OpenPushListener l) {
        mListener = l;
    }

    public synchronized void register() {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        if (mInitStatus == INIT_NOT_STARTED || mInitStatus == INIT_ERROR) {
            mInitStatus = INIT_IN_PROGRESS;

            PushProvider provider = getNextCandidate(null);
            if (provider != null
                    && provider.isAvailable()
                    && !provider.isRegistered()) {
                provider.register();
            } else {
                if (mListener != null) {
                    mListener.onNoAvailableProvider();
                }
                mInitStatus = INIT_ERROR;
            }
        } else {
            throw new IllegalStateException("Attempt to register twice!");
        }
    }

    public void unregister() {
        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        if (mCurrentProvider != null && mInitStatus == INIT_SUCCESS) {
            if (mPackageReceiver != null) {
                mAppContext.unregisterReceiver(mPackageReceiver);
            }
            mCurrentProvider.unregister();
            mCurrentProvider = null;
            mInitStatus = INIT_NOT_STARTED;
            saveProvider(null);
            mPreferences.edit().remove(KEY_INIT_STATUS).apply();
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
                IntentFilter packageRemovedIntentFilter
                        = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
                packageRemovedIntentFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
                packageRemovedIntentFilter.addDataPath(
                        provider.getHostAppPackage(), PatternMatcher.PATTERN_LITERAL);
                mPackageReceiver = new PackageChangeReceiver(provider);
                mAppContext.registerReceiver(mPackageReceiver, packageRemovedIntentFilter);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, String.format("Can not find package '%s'.",
                    provider.getHostAppPackage()), e);
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

    @MagicConstant(intValues = {INIT_ERROR, INIT_IN_PROGRESS, INIT_NOT_STARTED, INIT_SUCCESS})
    public int getInitStatus() {
        return mInitStatus;
    }

    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        if (mListener != null) {
            mListener.onMessage(providerName, extras);
        }
    }

    public void onUnregistered(@NotNull String providerName, @Nullable String registrationId) {
        if (mListener != null) {
            mListener.onUnregistered(providerName, registrationId);
        }
    }

    public void onDeletedMessages(@NotNull String providerName, @Nullable Bundle extras) {
        if (mListener != null) {
            mListener.onDeletedMessages(providerName, extras);
        }
    }

    void onHostAppRemoved(@NotNull PushProvider provider) {
        if (mCurrentProvider != null
                && mCurrentProvider.equals(provider)) {
            if (mListener != null) {
                mListener.onHostAppRemoved(provider.getName());
            }
            mInitStatus = INIT_NOT_STARTED;
            register();
        }
    }

    public void onRegistrationEnd(@NotNull RegistrationResult result) {
        if (mInitStatus != INIT_IN_PROGRESS) {
            return;
        }

        if (mOptions == null) {
            throw new UnsupportedOperationException("Before register provider call init().");
        }

        final PushProvider provider = getProviderByName(result.getProviderName());
        Assert.assertNotNull(provider);

        if (result.isSuccess()) {
            mInitStatus = INIT_SUCCESS;
            mCurrentProvider = provider;
            mPreferences.edit().putInt(KEY_INIT_STATUS, INIT_SUCCESS).apply();
            mRetryNumber = 0;
            saveProvider(mCurrentProvider);
            if (mListener != null) {
                mListener.onRegistered(result.getProviderName(), result.getRegistrationId());
            }

            registerPackageChangeReceiver(provider);
        } else {
            if (result.isRecoverableError() && mOptions.getBackoff() != null
                    && mRetryNumber < mOptions.getBackoff().tryCount()) {
                sHandler.postDelayed(new RetryRegistrationRunnable(provider),
                        mOptions.getBackoff().getDelay(mRetryNumber));
                mRetryNumber++;
            } else {
                if (mListener != null) {
                    mListener.onRegistrationError(provider.getName(), result.getErrorCode());
                }

                mRetryNumber = 0;
                PushProvider nextProvider = getNextCandidate(provider);
                if (nextProvider != null) {
                    nextProvider.register();
                } else {
                    mInitStatus = INIT_ERROR;
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
