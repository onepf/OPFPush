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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenPushHelper {

    private static final String PREFERENCES = "org.onepf.openpush.prefs";
    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";

    public static final int INIT_NOT_STARTED = 0;
    public static final int INIT_IN_PROGRESS = 1;
    public static final int INIT_SUCCESS = 2;
    public static final int INIT_ERROR = 3;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private int mInitStatus = INIT_NOT_STARTED;
    private Options mOptions;
    private Context mAppContext;
    private PushProvider mCurrentProvider;

    private int mRetryNumber = 0;

    private static OpenPushHelper sInstance;

    public static void sendMessage(@NotNull Context context,
                                   @NotNull String providerName,
                                   @Nullable Bundle extras) {
        sendBroadcast(context, OpenPushConstants.ACTION_MESSAGE, providerName, extras);
    }

    public static void sendMessageDeleted(@NotNull Context context,
                                          @NotNull String providerName,
                                          @Nullable Bundle extras) {
        sendBroadcast(context, OpenPushConstants.ACTION_MESSAGE_DELETED, providerName, extras);
    }

    public static void sendRegistered(@NotNull Context context,
                                      @NotNull String providerName,
                                      @Nullable String registrationId) {
        Bundle extras = new Bundle(1);
        extras.putString(OpenPushConstants.EXTRA_REGISTRATION_ID, registrationId);
        sendBroadcast(context, OpenPushConstants.ACTION_REGISTERED, providerName, extras);
    }

    public static void sendUnregistered(@NotNull Context context,
                                        @NotNull String providerName,
                                        @Nullable String registrationId) {
        Bundle extras = new Bundle(1);
        extras.putString(OpenPushConstants.EXTRA_REGISTRATION_ID, registrationId);
        sendBroadcast(context, OpenPushConstants.ACTION_UNREGISTERED, providerName, extras);
    }

    private static void sendBroadcast(@NotNull Context context,
                                      @NotNull String action,
                                      @NotNull String providerName,
                                      @Nullable Bundle extras) {
        Intent newIntent = new Intent(action);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        newIntent.putExtra(OpenPushConstants.EXTRA_PROVIDER_NAME, providerName);
        context.sendBroadcast(newIntent);
    }

    public static void notifyRegistrationEnd(@NotNull final ProviderRegistrationResult r) {
        if (sInstance != null && sInstance.mInitStatus == INIT_IN_PROGRESS) {
            sInstance.onRegistrationResult(r);
        } else {
            throw new UnsupportedOperationException(
                    "Can't post registration result when init isn't running.");
        }
    }

    public static OpenPushHelper getInstance(@NotNull Context context) {
        if (sInstance == null) {
            sInstance = new OpenPushHelper(context);
        }
        return sInstance;
    }

    private OpenPushHelper(@NotNull Context context) {
        mAppContext = context.getApplicationContext();
    }

    public synchronized void init(@NotNull Context context, @NotNull Options options) {
        if (mInitStatus == INIT_NOT_STARTED || mInitStatus == INIT_ERROR) {
            mInitStatus = INIT_IN_PROGRESS;
            mOptions = options;
            mAppContext = context.getApplicationContext();

            PushProvider provider = getLastProvider();
            if (provider != null && provider.isAvailable()) {
                mCurrentProvider = provider;
            } else {
                provider = getNextCandidate(null);
            }

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

    private void onRegistrationResult(@NotNull final ProviderRegistrationResult e) {
        if (e.getErrorCode() == OpenPushConstants.NO_ERROR) {
            mRetryNumber = 0;
            mCurrentProvider = getProviderByName(e.getProviderName());
            saveProvider(mCurrentProvider);
        } else {
            final PushProvider lastProvider = getProviderByName(e.getProviderName());
            if (mOptions.getRetryPolice() != null
                    && mRetryNumber < mOptions.getRetryPolice().tryCount()) {
                sHandler.postDelayed(new RetryRegistrationRunnable(lastProvider),
                        mOptions.getRetryPolice().getDelay(mRetryNumber));
                mRetryNumber++;
            } else {
                PushProvider provider = getNextCandidate(lastProvider);
                if (provider != null) {
                    provider.register();
                } else {
                    mInitStatus = INIT_ERROR;
                    //TODO Notify that can't register anyone of providers.
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
