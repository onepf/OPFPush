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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.onepf.openpush.exception.OpenPushException;

public class OpenPushHelper {

    public static final String ACTION_REGISTERED = "org.onepf.openpush.gcm.ACTION.registered";
    public static final String ACTION_UNREGISTERED = "org.onepf.openpush.ACTION.gcm.unregistered";
    public static final String ACTION_ERROR = "org.onepf.openpush.gcm.ACTION.error";
    public static final String ACTION_MESSAGE = "org.onepf.openpush.gcm.ACTION.message";

    public static final String EXTRA_MESSAGE = "org.onepf.openpush.EXTRA.message";
    public static final String EXTRA_TOKEN = "org.onepf.openpush.EXTRA.token";
    public static final String EXTRA_PROVIDER_NAME = "org.onepf.openpush.EXTRA.provider_name";
    public static final String EXTRA_REGISTRATION_ID = "org.onepf.openpush.EXTRA.registration_id";

    private static final String PREFERENCES = "org.onepf.openpush.preferences";
    private static final String KEY_PROVIDER_NAME = "org.onepf.openpush.provider";

    private boolean mInitialized;
    private Options mOptions;
    private Context mAppContext;
    private PushProvider mCurrentProvider;

    public static void sendMessage(@NonNull Context context,
                                   @NonNull String providerName,
                                   @Nullable Bundle extras) {
        Intent newIntent = new Intent(ACTION_MESSAGE);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        newIntent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        context.sendBroadcast(newIntent);
    }

    public static void sendError(@NonNull Context context,
                                 @NonNull String providerName,
                                 @Nullable Bundle extras) {
        Intent newIntent = new Intent(ACTION_ERROR);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        newIntent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        context.sendBroadcast(newIntent);
    }

    public static void sendRegistered(@NonNull Context context,
                                      @NonNull String providerName,
                                      @Nullable Bundle extras) {
        Intent newIntent = new Intent(ACTION_REGISTERED);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        newIntent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        context.sendBroadcast(newIntent);
    }

    public static void sendUnregistered(@NonNull Context context,
                                        @NonNull String providerName,
                                        @Nullable Bundle extras) {
        Intent newIntent = new Intent(ACTION_UNREGISTERED);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        newIntent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        context.sendBroadcast(newIntent);
    }

    public OpenPushHelper getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @NonNull
    public synchronized PushProvider init(@NonNull Context context, Options options) {
        if (mInitialized) {
            throw new IllegalStateException("Attempt to initialize OpenPushProvider twice!");
        } else {
            mOptions = options;
            mAppContext = context.getApplicationContext();

            PushProvider provider = getLastProvider();
            if (provider != null && provider.isAvailable()) {
                mCurrentProvider = provider;
            } else {
                for (PushProvider pushProvider : options.getProviders()) {
                    if (pushProvider.isAvailable()) {
                        provider = pushProvider;
                        break;
                    }
                }
            }

            if (provider == null) {
                throw new OpenPushException("All providers not available.");
            }

            mCurrentProvider = provider;
            mInitialized = true;
        }
        return mCurrentProvider;
    }

    public void start(@NonNull Context context, Options options) {
        PushProvider provider = init(context, options);
//            provider.register();
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

    public PushProvider getCurrentProvider() {
        if (!mInitialized) {
            throw new OpenPushException("Before get current provider call init() or start().");
        }
        return mCurrentProvider;
    }

    private static final class InstanceHolder {
        final static OpenPushHelper INSTANCE = new OpenPushHelper();
    }
}
