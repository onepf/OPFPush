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

package org.onepf.openpush.gcm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.ProviderRegistrationResult;
import org.onepf.openpush.exception.RegistrationException;
import org.onepf.openpush.exception.OpenPushException;

import java.io.IOException;

public class GoogleCloudMessagingProvider extends BasePushProvider {

    public static final String NAME = "com.google.android.gms.gcm.provider";

    private static final String GCM_PREFERENCES_NAME = "GCM_PREFS";
    private static final String PREF_REGISTRATION_ID = "registration_id";
    private static final String PREF_APP_VERSION = "appVersion";

    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String ANDROID_RELEASE_4_0_4 = "4.0.4";

    private String mRegistrationId;
    private final String mSenderId;

    public GoogleCloudMessagingProvider(@NotNull Context context, @NotNull String senderID) {
        super(context, "com.google.android.gms.gcm.GoogleCloudMessaging");
        mSenderId = senderID;

        final SharedPreferences prefs = getPreferences();
        if (prefs.contains(PREF_REGISTRATION_ID)) {
            mRegistrationId = prefs.getString(PREF_REGISTRATION_ID, null);
        }
    }

    /**
     * Get version code of current application.
     *
     * @return If find app - return it's version code, else {@link java.lang.Integer#MIN_VALUE}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
        } catch (NullPointerException npe) {
            // a very rare case that we cannot process correctly
        }
        return Integer.MIN_VALUE;
    }

    private SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(GCM_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void register() {
        if (isRegistered()) {
            throw new OpenPushException("Provider already registered.");
        }

        try {
            mRegistrationId = GoogleCloudMessaging.getInstance(getContext()).register(mSenderId);
            saveRegistrationId(this.mRegistrationId);
            OpenPushHelper.notifyRegistrationEnd(
                    new ProviderRegistrationResult(GoogleCloudMessagingProvider.NAME, mRegistrationId)
            );
        } catch (IOException e) {
            ProviderRegistrationResult result;
            if (e.getMessage().equals(GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE)) {
                result = new ProviderRegistrationResult(NAME,
                        ProviderRegistrationResult.ERROR_SERVICE_NOT_AVAILABLE);
            } else {
                result = new ProviderRegistrationResult(NAME, ProviderRegistrationResult.ERROR_UNKNOWN);
            }
            OpenPushHelper.notifyRegistrationEnd(result);
        }
    }

    public void unregister() throws RegistrationException {
        if (isRegistered()) {
            try {
                GoogleCloudMessaging.getInstance(getContext()).unregister();
                mRegistrationId = null;
            } catch (IOException e) {
                throw new RegistrationException("Error unregister Google Cloud Messaging.", e);
            }
        } else {
            throw new OpenPushException("Provider must be registered before unregister.");
        }
    }

    @Override
    public String getRegistrationId() {
        return mRegistrationId;
    }

    @Override
    public boolean isAvailable() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext()) == ConnectionResult.SUCCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    || Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4)) {
                return true;
            } else {
                // On device with version of Android less than "4.0.4"
                // we need to ensure that user has at least one google account.
                Account[] googleAccounts = AccountManager.get(getContext())
                        .getAccountsByType(GOOGLE_ACCOUNT_TYPE);
                return googleAccounts.length != 0;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isRegistered() {
        if (TextUtils.isEmpty(mRegistrationId)) {
            return false;
        } else {
            SharedPreferences prefs = getPreferences();
            if (prefs.contains(PREF_APP_VERSION)) {
                int registeredVersion = prefs.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
                int currentVersion = getAppVersion();
                return registeredVersion > 0 && registeredVersion == currentVersion;
            } else {
                return false;
            }
        }
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param registrationId registration ID
     */
    private void saveRegistrationId(@Nullable String registrationId) {
        SharedPreferences.Editor editor = getPreferences().edit();
        if (TextUtils.isEmpty(registrationId)) {
            editor.remove(PREF_REGISTRATION_ID);
        } else {
            editor.putString(PREF_REGISTRATION_ID, registrationId);
        }

        editor.putInt(PREF_APP_VERSION, getAppVersion());
        editor.apply();
    }

    @NotNull
    @Override
    public String getHostAppPackage() {
        return "com.android.vending";
    }
}
