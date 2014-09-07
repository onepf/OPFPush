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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushException;
import org.onepf.openpush.util.PackageUtils;

import java.io.IOException;

public class GCMProvider extends BasePushProvider {

    public static final String NAME = "com.google.android.gms.gcm.provider";
    private static final String PREF_REGISTRATION_TOKEN = "registration_token";
    private static final String PREF_APP_VERSION = "app_version";
    private static final String PREF_ANDROID_ID = "android_id";

    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String ANDROID_RELEASE_4_0_4 = "4.0.4";

    private String mRegistrationToken;
    private final String mSenderId;

    private final SharedPreferences mPreferences;

    public GCMProvider(@NotNull Context context, @NotNull String senderID) {
        super(context, "com.google.android.gms.gcm.GoogleCloudMessaging");
        Assert.assertNotNull(senderID);
        mSenderId = senderID;

        mPreferences = context.getSharedPreferences(
                String.format("gcm_prefs_%s", senderID),
                Context.MODE_PRIVATE
        );
        if (mPreferences.contains(PREF_REGISTRATION_TOKEN)) {
            mRegistrationToken = mPreferences.getString(PREF_REGISTRATION_TOKEN, null);
        }
    }

    public void register() {
        if (isRegistered()) {
            throw new OpenPushException("Provider already registered.");
        }
        new RegisterTask().execute();
    }

    public void unregister() {
        if (isRegistered()) {
            new UnregisterTask().execute();
        } else {
            throw new OpenPushException("Provider must be registered before unregister.");
        }
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return mRegistrationToken;
    }

    @Override
    public boolean isAvailable() {
        if (super.isAvailable()) {
            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext())
                    == ConnectionResult.SUCCESS) {

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
        } else {
            return false;
        }
    }

    @Override
    public boolean isRegistered() {
        if (TextUtils.isEmpty(mRegistrationToken)) {
            return false;
        } else {
            if (mPreferences.contains(PREF_APP_VERSION)) {
                int registeredVersion = mPreferences.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
                int currentVersion = PackageUtils.getAppVersion(getContext());
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

    @NotNull
    @Override
    public String getHostAppPackage() {
        return "com.android.vending";
    }

    @Override
    public String toString() {
        return String.format("%s (senderId: '%s', appVersion: %d)", NAME, mSenderId,
                mPreferences.getInt(PREF_APP_VERSION, -1));
    }

    private class UnregisterTask extends AsyncTask<Void, Void, Boolean> {
        private String mOldRegistrationToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mOldRegistrationToken = mRegistrationToken;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                GoogleCloudMessaging.getInstance(getContext()).unregister();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mRegistrationToken = null;
                mPreferences.edit()
                        .remove(PREF_APP_VERSION)
                        .remove(PREF_REGISTRATION_TOKEN)
                        .apply();

                Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION);
                intent.putExtra(GCMConstants.EXTRA_TOKEN, mOldRegistrationToken);
                getContext().sendBroadcast(intent);
            } else {
                //TODO Send error.
            }
        }
    }

    private class RegisterTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreferences.edit()
                    .remove(PREF_ANDROID_ID)
                    .remove(PREF_APP_VERSION)
                    .remove(PREF_REGISTRATION_TOKEN)
                    .apply();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return GoogleCloudMessaging.getInstance(getContext()).register(mSenderId);
            } catch (IOException e) {
                Intent intent = new Intent(GCMConstants.ACTION_ERROR);
                if (GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE.equals(e.getMessage())) {
                    intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                            GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
                }
                getContext().sendBroadcast(intent);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String registrationId) {
            mRegistrationToken = registrationId;
            if (mRegistrationToken != null) {
                mPreferences.edit()
                        .putString(PREF_ANDROID_ID, Settings.Secure.ANDROID_ID)
                        .putString(PREF_REGISTRATION_TOKEN, registrationId)
                        .putInt(PREF_APP_VERSION, PackageUtils.getAppVersion(getContext()))
                        .apply();

                Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION);
                intent.putExtra(GCMConstants.EXTRA_TOKEN, mRegistrationToken);
                getContext().sendBroadcast(intent);
            } else {
                Intent intent = new Intent(GCMConstants.ACTION_ERROR);
                intent.putExtra(GCMConstants.EXTRA_ERROR_ID, GCMConstants.ERROR_AUTHEFICATION_FAILED);
                getContext().sendBroadcast(intent);
            }
        }
    }
}
