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
import org.onepf.openpush.exception.OpenPushException;

public class OpenPushHelper {

    public static final String ACTION_REGISTERED =
            "org.onepf.openpush.gcm.ACTION.registered";
    public static final String ACTION_UNREGISTERED =
            "org.onepf.openpush.ACTION.gcm.unregistered";
    public static final String ACTION_MESSAGE =
            "org.onepf.openpush.gcm.ACTION.message";
    public static final String ACTION_MESSAGE_DELETED =
            "org.onepf.openpush.gcm.ACTION.message_deleted";
    public static final String ACTION_HOST_APP_REMOVED
            = "org.onepf.openpush.gcm.ACTION.host_app_removed";

    public static final String EXTRA_PROVIDER_NAME
            = "org.onepf.openpush.EXTRA.provider_name";
    public static final String EXTRA_REGISTRATION_ID
            = "org.onepf.openpush.EXTRA.registration_id";
    public static final String EXTRA_MESSAGES_COUNT
            = "org.onepf.openpush.EXTRA.messages_count";
    public static final String EXTRA_HOST_APP_PACKAGE
            = "org.onepf.openpush.EXTRA.messages_count";

    private static final String PREFERENCES = "org.onepf.openpush.preferences";
    private static final String KEY_PROVIDER_NAME = "org.onepf.openpush.provider";

    private static final int INIT_NOT_STARTED = 0;
    private static final int INIT_IN_PROGRESS = 1;
    private static final int INIT_SUCCESS = 2;
    private static final int INIT_ERROR = 3;

    private int mInitStatus = INIT_NOT_STARTED;
    private Options mOptions;
    private Context mAppContext;
    private PushProvider mCurrentProvider;

    private int mRetryNumber = 0;

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static void sendMessage(@NotNull Context context,
                                   @NotNull String providerName,
                                   @Nullable Bundle extras) {
        sendBroadcast(context, ACTION_MESSAGE, providerName, extras);
    }

    public static void sendMessageDeleted(@NotNull Context context,
                                          @NotNull String providerName,
                                          @Nullable Bundle extras) {
        sendBroadcast(context, ACTION_MESSAGE_DELETED, providerName, extras);
    }

    public static void sendRegistered(@NotNull Context context,
                                      @NotNull String providerName,
                                      @Nullable Bundle extras) {
        sendBroadcast(context, ACTION_REGISTERED, providerName, extras);
    }

    public static void sendUnregistered(@NotNull Context context,
                                        @NotNull String providerName,
                                        @Nullable Bundle extras) {
        sendBroadcast(context, ACTION_UNREGISTERED, providerName, extras);
    }

    private static void sendBroadcast(@NotNull Context context,
                                      @NotNull String action,
                                      @NotNull String providerName,
                                      @Nullable Bundle extras) {
        Intent newIntent = new Intent(action);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        newIntent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        context.sendBroadcast(newIntent);
    }

    public static void notifyRegistrationEnd(@NotNull final ProviderRegistrationResult r) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                OpenPushHelper instance = OpenPushHelper.getInstance();
                if (instance.mInitStatus == INIT_IN_PROGRESS) {
                    instance.onRegistrationResult(r);
                } else {
                    throw new UnsupportedOperationException("Can't post registration result when int");
                }
            }
        });
    }

    public static OpenPushHelper getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public synchronized void init(@NotNull Context context, @NotNull Options options) {
        if (mInitStatus == INIT_NOT_STARTED
                || mInitStatus == INIT_ERROR) {
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
                new ProviderRegistrationTask().execute(provider);
            }
        } else {
            throw new IllegalStateException("Attempt to initialize OpenPushProvider twice!");
        }
    }

    @Nullable
    private PushProvider getNextCandidate(@Nullable PushProvider lastCandidate) {
        int i = 0;
        if (lastCandidate != null) {
            int lastCandidateIndex = mOptions.getProviders().indexOf(lastCandidate);
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
        if (e.getErrorCode() == ProviderRegistrationResult.NO_ERROR) {
            mRetryNumber = 0;
            mCurrentProvider = getProviderByName(e.getProviderName());
            if (mCurrentProvider != null) {
                Bundle extras = new Bundle(1);
                extras.putString(EXTRA_REGISTRATION_ID, e.getRegistrationId());
                sendRegistered(mAppContext, mCurrentProvider.getName(), extras);
            }
        } else {
            final PushProvider lastProvider = getProviderByName(e.getProviderName());
            if (mOptions.getRetryPolice() != null
                    && mRetryNumber < mOptions.getRetryPolice().tryCount()) {
                HANDLER.postDelayed(new RetryRegistrationRunnable(lastProvider),
                        mOptions.getRetryPolice().getDelay(mRetryNumber));
                mRetryNumber++;
            } else {
                PushProvider provider = getNextCandidate(lastProvider);
                if (provider != null) {
                    new ProviderRegistrationTask().execute(provider);
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
        final String storedProviderName = prefs.getString(KEY_PROVIDER_NAME, null);

        if (storedProviderName != null) {
            for (PushProvider provider : mOptions.getProviders()) {
                if (storedProviderName.equals(provider.getName())) {
                    return provider;
                }
            }
        }
        return null;
    }

    @MagicConstant(intValues = {INIT_ERROR, INIT_IN_PROGRESS, INIT_NOT_STARTED, INIT_SUCCESS})
    public int getInitStatus() {
        return mInitStatus;
    }

    public PushProvider getCurrentProvider() {
        if (mInitStatus != INIT_SUCCESS) {
            throw new OpenPushException("Before get current provider call init().");
        }
        return mCurrentProvider;
    }

    private static final class InstanceHolder {
        final static OpenPushHelper INSTANCE = new OpenPushHelper();
    }

    private static class RetryRegistrationRunnable implements Runnable {
        @NotNull
        private final PushProvider mLastProvider;

        public RetryRegistrationRunnable(@NotNull PushProvider lastProvider) {
            mLastProvider = lastProvider;
        }

        @Override
        public void run() {
            new ProviderRegistrationTask().execute(mLastProvider);
        }
    }
}
