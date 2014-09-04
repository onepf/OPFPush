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
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * @author Alexey Vitenko, Anton Rutkevich
 * @since 14.05.14
 */
public class OpenPushProvider implements PushProvider {
    private static final String TAG = OpenPushProvider.class.getSimpleName();

    private static final String PREFERENCES = OpenPushProvider.class.getPackage().getName() + ".preferences";
    private static final String KEY_PROVIDER_NAME = OpenPushProvider.class.getPackage().getName() + ".provider";

    private boolean mIsInitialized;

    private InternalPushListener mInternalListener;

    private Context context;

    private PushProvider currentProvider;

    //region Singleton implementation
    private static class SingletonHolder {
        private static final OpenPushProvider INSTANCE = new OpenPushProvider();
    }

    public static OpenPushProvider getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private OpenPushProvider() {
        if (OpenPushLog.isEnabled()) {
            mInternalListener = new LoggingInternalPushListener();
        } else {
            mInternalListener = new InternalPushListener();
        }
    }

    //endregion

    /**
     * Explicitly initialize helper.
     * This method will choose best provider according to {@code addProvider} calls order.
     *
     * @param config
     */
    public synchronized void init(Options config) {
        initInternal(config);
    }

    PushListener getInternalListener() {
        return mInternalListener;
    }

    private synchronized void initInternal(Options config) {
        if (!mIsInitialized) {

            if (OpenPushLog.isEnabled()) {
                Log.i(TAG, "Initializing...");
            }

//            mInternalListener.setExternalListener(config.pushListener);
//
//            context = config.appContext;

            List<PushProvider> mProviders = config.getProviders();
            currentProvider = loadStoredProvider(context, mProviders);
            if (currentProvider == null || !currentProvider.available()) {
                currentProvider = chooseProvider(mProviders);
            }

            mIsInitialized = true;
        } else {
            if (OpenPushStrictMode.isEnabled()) {
                throw new IllegalStateException("Attempt to initialize OpenPushProvider twice!");
            }
        }
    }

    private PushProvider chooseProvider(List<PushProvider> providers) {
        for (PushProvider provider : providers) {
            if (provider.available()) {
                if (OpenPushLog.isEnabled()) {
                    Log.i(TAG, "Chosen provider: " + provider.getName());
                }
                return provider;
            }
        }

        return null;
    }

    private PushProvider loadStoredProvider(Context context, List<PushProvider> mProviders) {
        String storedProviderName = loadStoredProviderName(context);
        if (OpenPushLog.isEnabled()) {
            Log.i(TAG, "Last stored provider: " + storedProviderName);
        }

        if (storedProviderName != null) {
            for (PushProvider provider : mProviders) {
                if (storedProviderName.equals(provider.getName())) {
                    if (OpenPushLog.isEnabled()) {
                        Log.i(TAG, "Selected provider: " + provider.getName());
                    }
                    return provider;
                }
            }
        }

        return null;
    }

    private String loadStoredProviderName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PROVIDER_NAME, null);
    }

    private void storeCurrentProvider(Context context, String providerName) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
                .putString(KEY_PROVIDER_NAME, providerName)
                .apply();
    }

    //region PushProvider implementation
    @Override
    public boolean available() {
        return currentProvider != null;
    }

    @Override
    public boolean isRegistered() {
        return available() && currentProvider.isRegistered();
    }

    @Override
    public String getRegistrationId() {
        if (available()) {
            return currentProvider.getRegistrationId();
        } else {
            return null;
        }
    }

    @Override
    public void register() {
        if (available()) {
            currentProvider.register();
        }
    }

    @Override
    public void unregister() {
        if (available()) {
            currentProvider.unregister();
        }
    }

    @Override
    public String getName() {
        if (available()) {
            return currentProvider.getName();
        }

        return null;
    }

    //endregion

    private class InternalPushListener extends PushListener {

        private PushListener mExternalListener;

        void setExternalListener(PushListener mExternalListener) {
            this.mExternalListener = mExternalListener;
        }

        @Override
        public void onMessage(Context context, Bundle extras, String providerName) {
            if (isFromCurrentProvider(providerName)) {
                mExternalListener.onMessage(context, extras, providerName);
            }
        }

        @Override
        public void onRegistered(Context context, String token, String providerName) {
            storeCurrentProvider(context, providerName);

            if (isFromCurrentProvider(providerName)) {
                mExternalListener.onRegistered(context, token, providerName);
            }
        }

        @Override
        public void onUnregistered(Context context, String token, String providerName) {
            if (isFromCurrentProvider(providerName)) {
                mExternalListener.onUnregistered(context, token, providerName);
            }
        }

        @Override
        public void onError(Context context, String message, String providerName) {
            if (isFromCurrentProvider(providerName)) {
                mExternalListener.onError(context, message, providerName);
            }
        }

        private boolean isFromCurrentProvider(String providerName) {
            return mExternalListener != null && currentProvider.getName().equals(providerName);
        }
    }

    private class LoggingInternalPushListener extends InternalPushListener {

        private final String TAG = "InternalPushListener";

        @Override
        public void onMessage(Context context, Bundle extras, String providerName) {
            Log.d(TAG, "onMessage. Provider: " + providerName + ", extras: " + OpenPushLog.bundleToString(extras));
            super.onMessage(context, extras, providerName);
        }

        @Override
        public void onRegistered(Context context, String token, String providerName) {
            Log.d(TAG, "onRegistered. Provider: " + providerName + ", token: " + token);
            super.onRegistered(context, token, providerName);
        }

        @Override
        public void onUnregistered(Context context, String token, String providerName) {
            Log.d(TAG, "onUnregistered. Provider: " + providerName + ", oldToken: " + token);
            super.onUnregistered(context, token, providerName);
        }

        @Override
        public void onError(Context context, String message, String providerName) {
            Log.d(TAG, "onError. Provider: " + providerName + ", error: " + message);
            super.onError(context, message, providerName);
        }

    }
}
