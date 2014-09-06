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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushException;

import java.io.IOException;

public class GCMProvider extends BasePushProvider {

    public static final String NAME = "com.google.android.gms.gcm.provider";

    private static final String GCM_PREFERENCES_NAME = "GCM_PREFS";
    private static final String PREF_REGISTRATION_ID_FORMAT = "sender_%s.registration_id";
    private static final String PREF_APP_VERSION = "appVersion";

    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String ANDROID_RELEASE_4_0_4 = "4.0.4";

    private String mRegistrationId;
    private final String mSenderId;
    private final String mPrefRegistrationId;

    public GCMProvider(@NotNull Context context, @NotNull String senderID) {
        super(context, "com.google.android.gms.gcm.GoogleCloudMessaging");
        mSenderId = senderID;
        mPrefRegistrationId = String.format(PREF_REGISTRATION_ID_FORMAT, mSenderId);

        final SharedPreferences prefs = getPreferences();
        if (prefs.contains(mPrefRegistrationId)) {
            mRegistrationId = prefs.getString(mPrefRegistrationId, null);
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

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String mRegistrationId = GoogleCloudMessaging.getInstance(getContext())
                            .register(mSenderId);
                    saveRegistrationId(mRegistrationId);
                    if (mRegistrationId != null) {
                        Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION);
                        intent.putExtra(GCMConstants.EXTRA_TOKEN, mRegistrationId);
                        getContext().sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(GCMConstants.ACTION_ERROR);
                        intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                                GCMConstants.ERROR_AUTHEFICATION_FAILED);
                        getContext().sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    Intent intent = new Intent(GCMConstants.ACTION_ERROR);
                    if (GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE.equals(e.getMessage())) {
                        intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                                GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
                    }
                    getContext().sendBroadcast(intent);
                }
                return null;
            }
        }.execute();
    }

    public void unregister() {
        if (isRegistered()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        GoogleCloudMessaging.getInstance(getContext()).unregister();
                        String oldRegistrationToken = mRegistrationId;
                        mRegistrationId = null;

                        Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION);
                        intent.putExtra(GCMConstants.EXTRA_TOKEN, oldRegistrationToken);
                        getContext().sendBroadcast(intent);
                    } catch (IOException e) {
                        //TODO Send error.
                    }
                    return null;
                }
            }.execute();
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
            editor.remove(mPrefRegistrationId);
        } else {
            editor.putString(mPrefRegistrationId, registrationId);
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
