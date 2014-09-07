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
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import junit.framework.Assert;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenPushHelper {

    private static final String PREFERENCES = "org.onepf.openpush.prefs";
    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_INIT_STATUS = "init_status";

    public static final int INIT_NOT_STARTED = 0;
    public static final int INIT_IN_PROGRESS = 1;
    public static final int INIT_SUCCESS = 2;
    public static final int INIT_ERROR = 3;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private int mInitStatus;
    private Options mOptions;
    private Context mAppContext;
    private PushProvider mCurrentProvider;

    private int mRetryNumber = 0;

    private static OpenPushHelper sInstance;
    private static OpenPushListener sListener;

    public static void notifyRegistrationEnd(@NotNull final ProviderRegistrationResult r) {
        if (sInstance != null && sInstance.mInitStatus == INIT_IN_PROGRESS) {
            sInstance.onRegistrationResult(r);
        } else {
            throw new UnsupportedOperationException(
                    "Can't post registration result when init isn't running.");
        }
    }

    public static OpenPushListener getListener() {
        return sListener;
    }

    public static OpenPushHelper getInstance(@NotNull Context context) {
        if (sInstance == null) {
            sInstance = new OpenPushHelper(context);
        }
        return sInstance;
    }

    private OpenPushHelper(@NotNull Context context) {
        mAppContext = context.getApplicationContext();
        sListener = new OpenPushListener(mAppContext);
        mInitStatus = getSavedInitStatus();

        PushProvider provider = getLastProvider();
        if (provider != null && provider.isAvailable()
                && mInitStatus == INIT_SUCCESS) {
            mCurrentProvider = provider;
        }
    }

    private int getSavedInitStatus() {
        return mAppContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getInt(KEY_INIT_STATUS, INIT_NOT_STARTED);
    }

    private void saveInitStatus(int initStatus) {
        mAppContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_INIT_STATUS, initStatus)
                .apply();
    }

    public synchronized void init(@NotNull Options options) {
        if (mInitStatus == INIT_NOT_STARTED || mInitStatus == INIT_ERROR) {
            mInitStatus = INIT_IN_PROGRESS;
            mOptions = options;

            PushProvider provider = getNextCandidate(null);
            if (provider != null) {
                provider.register();
            }
        } else {
            throw new IllegalStateException("Attempt to initialize OpenPushProvider twice!");
        }
    }

    @Nullable
    private PushProvider getNextCandidate(@Nullable PushProvider lastProvider) {
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

    private void onRegistrationResult(@NotNull final ProviderRegistrationResult result) {
        final PushProvider provider = getProviderByName(result.getProviderName());
        Assert.assertNotNull(provider);

        if (result.isSuccess()) {
            mInitStatus = INIT_SUCCESS;
            saveInitStatus(INIT_SUCCESS);
            mRetryNumber = 0;
            mCurrentProvider = provider;
            saveProvider(mCurrentProvider);
            sListener.onRegistered(result.getProviderName(), result.getRegistrationId());
        } else {
            if (result.isRecoverableError() && mOptions.getRetryPolice() != null
                    && mRetryNumber < mOptions.getRetryPolice().tryCount()) {
                sHandler.postDelayed(new RetryRegistrationRunnable(provider),
                        mOptions.getRetryPolice().getDelay(mRetryNumber));
                mRetryNumber++;
            } else {
                sListener.onRegistrationError(provider.getName(), result.getErrorCode());

                mRetryNumber = 0;
                PushProvider nextProvider = getNextCandidate(provider);
                if (nextProvider != null) {
                    nextProvider.register();
                } else {
                    mInitStatus = INIT_ERROR;
                    sListener.onNoAvailableProvider();
                }
            }
        }
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
        SharedPreferences prefs = mAppContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        final String storedProviderName = prefs.getString(KEY_LAST_PROVIDER_NAME, null);

        if (storedProviderName != null) {
            for (PushProvider provider : mOptions.getProviders()) {
                if (storedProviderName.equals(provider.getName())) {
                    return provider;
                }
            }
        }
        return null;
    }

    private void saveProvider(PushProvider provider) {
        mAppContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAST_PROVIDER_NAME, provider.getName())
                .apply();
    }

    @MagicConstant(intValues = {INIT_ERROR, INIT_IN_PROGRESS, INIT_NOT_STARTED, INIT_SUCCESS})
    public int getInitStatus() {
        return mInitStatus;
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
