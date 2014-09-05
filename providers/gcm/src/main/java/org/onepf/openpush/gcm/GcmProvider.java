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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.exception.RegistrationException;
import org.onepf.openpush.exception.OpenPushException;

import java.io.IOException;

public class GcmProvider extends BasePushProvider {

    public static final String NAME = "com.google.android.gms.gcm.provider";
    private static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";
    private static final String IMPLEMENTATION_CLASS_NAME
            = "com.google.android.gms.gcm.GoogleCloudMessaging";

    private static final String GCM_PREFERENCES_NAME = "GCM_PREFS";
    private static final String PREF_REGISTRATION_ID = "registration_id";
    private static final String PREF_APP_VERSION = "appVersion";

    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String ANDROID_RELEASE_4_0_4 = "4.0.4";

    private String mRegistrationId;
    private String mSenderId;

    public GcmProvider(@NonNull Context context, @NonNull String senderID) {
        super(context, IMPLEMENTATION_CLASS_NAME);
        mSenderId = senderID;
        loadLocalRegistrationId();
    }

    private static int getAppVersion(@NonNull Context context, @NonNull String appPackage) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(appPackage, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
        } catch (NullPointerException npe) {
            // a very rare case that we cannot process correctly
        }
        return Integer.MIN_VALUE;
    }

    static SharedPreferences getGcmPreferences(@NonNull Context context) {
        return context.getSharedPreferences(GCM_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void register() throws RegistrationException {
        if (isRegistered()) {
            throw new OpenPushException("Provider already registered.");
        }

        loadLocalRegistrationId();
        try {
            String registrationId = GoogleCloudMessaging.getInstance(getContext()).register(mSenderId);
            setRegistrationId(registrationId);
        } catch (IOException e) {
            throw new RegistrationException("Error register GCM.", e);
        }

        Bundle extras = new Bundle();
        extras.putString(OpenPushHelper.EXTRA_REGISTRATION_ID, mRegistrationId);
        OpenPushHelper.sendRegistered(getContext(), GcmProvider.NAME, extras);
    }

    public void unregister() throws RegistrationException {
        if (isRegistered()) {
            try {
                GoogleCloudMessaging.getInstance(getContext()).unregister();
                getRegistrationId();
                removeRegistrationId();
            } catch (IOException e) {
                throw new RegistrationException("Error unregister GCM.", e);
            }

            Bundle extras = new Bundle();
            extras.putString(OpenPushHelper.EXTRA_REGISTRATION_ID, mRegistrationId);
            OpenPushHelper.sendUnregistered(getContext(), GcmProvider.NAME, extras);
        } else {
            throw new OpenPushException("Provider must be registered before unregister.");
        }
    }

    private void removeRegistrationId() {
        setRegistrationId(null);
    }

    @Override
    public String getRegistrationId() {
        return mRegistrationId;
    }

    private void setRegistrationId(String registrationId) {
        mRegistrationId = registrationId;
        storeRegistrationId(getContext(), this.mRegistrationId);
    }

    @Override
    public boolean isAvailable() {
        if (isPlayServicesAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    || Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4)) {
                return true;
            } else {
                // On device with version of Android lower than "4.0.4"
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
        return !TextUtils.isEmpty(mRegistrationId);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    private boolean isPlayServicesAvailable() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext())
                == ConnectionResult.SUCCESS;
    }

    private void loadLocalRegistrationId() {
        Context context = getContext();
        final SharedPreferences prefs = getGcmPreferences(context);
        if (prefs.contains(PREF_REGISTRATION_ID)) {
            mRegistrationId = prefs.getString(PREF_REGISTRATION_ID, null);
        }

        if (prefs.contains(PREF_APP_VERSION)) {
            int registeredVersion = prefs.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
            int currentVersion = getAppVersion(context, context.getPackageName());
            if (registeredVersion != currentVersion) {
                mRegistrationId = "";
            }
        } else {
            mRegistrationId = "";
        }
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context        application's context.
     * @param registrationId registration ID
     */
    private void storeRegistrationId(@NonNull Context context, @NonNull String registrationId) {
        getGcmPreferences(context).edit()
                .putString(PREF_REGISTRATION_ID, registrationId)
                .putInt(PREF_APP_VERSION, getAppVersion(context, context.getPackageName()))
                .apply();
    }

    @NonNull
    @Override
    public String getHostAppPackage() {
        return GOOGLE_PLAY_PACKAGE;
    }
}
