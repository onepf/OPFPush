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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushLog;

import java.io.IOException;

/*
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class GcmProvider extends BasePushProvider {

    public static final String NAME = "com.google.android.gms.gcm.provider";

    private static final String IMPLEMENTATION_CLASS_NAME = "com.google.android.gms.gcm.GoogleCloudMessaging";

    private static final String TAG = GcmProvider.class.getSimpleName();

    private static final String GCM_PREFERENCES_NAME = "GCM_PREFS";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String EMPTY_STRING = "";


    private static final String BACKOFF_MS = "backoff_ms";

    private Context mApplicationContext;

    private String registrationId;
    private String senderId;

    /**
     *
     * @param context
     * @param senderID
     * @hide
     */
    public GcmProvider(Context context, String senderID) {
        super(IMPLEMENTATION_CLASS_NAME);

        mApplicationContext = context.getApplicationContext();
        senderId = senderID;
        loadLocalRegistrationId();
    }

    public void register() {
        loadLocalRegistrationId();
        if (!isRegistered()) {
            registerInBackground(senderId);
        }
    }

    public void unregister() {
        if (isRegistered()) {
            unregisterInBackground();
        }
    }

    public Dialog getRecoverGooglePlayServicesDialog(Activity context, int errorCode) {
        return GooglePlayServicesUtil.getErrorDialog(errorCode, context,
                                              PLAY_SERVICES_RESOLUTION_REQUEST);
    }

    private void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;

        storeRegistrationId(mApplicationContext, this.registrationId);
    }

    private void removeRegistrationId() {
        setRegistrationId(null);
    }

    @Override
    public String getRegistrationId() {
        return registrationId;
    }

    @Override
    public boolean available() {
        return checkPlayServices();
    }

    @Override
    public boolean isRegistered() {
        return !TextUtils.isEmpty(registrationId);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mApplicationContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode) && !hasGooglePlay(mApplicationContext)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasGooglePlay(Context context) {
        return getAppVersion(context, GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE) > 0;
    }

    /**
     * Loads the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     */
    private void loadLocalRegistrationId() {
        final SharedPreferences prefs = getGcmPreferences(mApplicationContext);
        registrationId = prefs.getString(PROPERTY_REG_ID, EMPTY_STRING);
        if (registrationId.length() == 0) {
            if (OpenPushLog.isEnabled()) {
                Log.i(TAG, "Registration not found.");
            }
            registrationId = EMPTY_STRING;
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(mApplicationContext, mApplicationContext.getPackageName());
        if (registeredVersion != currentVersion) {
            if (OpenPushLog.isEnabled()) {
                Log.i(TAG, "App version changed.");
            }
            registrationId = EMPTY_STRING;
        }
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context, context.getPackageName());
        if (OpenPushLog.isEnabled()) {
            Log.i(TAG, "Saving registrationId on app version " + appVersion);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final String senderId) {
        new GcmRegistrationTask(this, senderId).execute();
    }

    private void unregisterInBackground() {
        new GcmUnregistrationTask(this).execute();
    }

    private void notifyWorkflowListeners(String eventType, String message) {
        Intent intent = new Intent(eventType);
        intent.setClass(mApplicationContext, GcmIntentService.class);
        intent.putExtra(GcmIntentService.EXTRA_MESSAGE, message);
        mApplicationContext.startService(intent);
    }

    private static int getAppVersion(Context context, String appPackage) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(appPackage, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
        } catch (NullPointerException npe) {
            // a very rare case that we cannot process correctly
        }

        return -1;
    }


    /**
     * Resets the backoff counter.
     * <p/>
     * This method should be called after a GCM call succeeds.
     *
     * @param context application's context.
     */
    static void resetBackoff(Context context, int defaultValue) {
        if (OpenPushLog.isEnabled()) {
            Log.d(TAG, "resetting backoff for " + context.getPackageName());
        }
        setBackoff(context, defaultValue);
    }

    /**
     * Gets the current backoff counter.
     *
     * @param context application's context.
     * @return current backoff counter, in milliseconds.
     */
    static int getBackoff(Context context, int defaultValue) {
        final SharedPreferences prefs = getGcmPreferences(context);
        return prefs.getInt(BACKOFF_MS, defaultValue);
    }

    /**
     * Sets the backoff counter.
     * <p/>
     * This method should be called after a GCM call fails, passing an
     * exponential value.
     *
     * @param context application's context.
     * @param backoff new backoff counter, in milliseconds.
     */
    static void setBackoff(Context context, int backoff) {
        final SharedPreferences prefs = getGcmPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(BACKOFF_MS, backoff);
        editor.commit();
    }

    static SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences(GCM_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static class GcmRegistrationTask extends GcmTask {

        private String mSenderId;

        private GcmRegistrationTask(GcmProvider provider, String senderId) {
            super(provider);
            mSenderId = senderId;
        }

        String performGcmInteraction() throws IOException {
            GcmProvider provider = getProvider();
            String registrationId = null;
            if (provider != null) {
                registrationId = GoogleCloudMessaging.getInstance(provider.mApplicationContext).register(mSenderId);
                provider.setRegistrationId(registrationId);
            }

            return registrationId;
        }

        void notifyListenersOnSuccess(String result) {
            GcmProvider provider = getProvider();
            if (provider != null) {
                provider.notifyWorkflowListeners(GcmIntentService.ACTION_REGISTRATION, result);
            }
        }
    }

    private static class GcmUnregistrationTask extends GcmTask {

        private GcmUnregistrationTask(GcmProvider provider) {
            super(provider);
        }

        String performGcmInteraction() throws IOException {
            GcmProvider provider = getProvider();
            String id = null;
            if (provider != null) {
                GoogleCloudMessaging.getInstance(provider.mApplicationContext).unregister();
                id = provider.getRegistrationId();
                provider.removeRegistrationId();
            }

            return id;
        }

        void notifyListenersOnSuccess(String result) {
            GcmProvider provider = getProvider();
            if (provider != null) {
                provider.notifyWorkflowListeners(GcmIntentService.ACTION_UNREGISTRATION, result);
            }
        }
    }

    private abstract static class GcmTask extends AsyncTask<Void, Void, Void> {

        private GcmProvider mProvider;

        private GcmTask(GcmProvider provider) {
            mProvider = provider;
        }

        GcmProvider getProvider() {
            return mProvider;
        }

        abstract void notifyListenersOnSuccess(String result);

        abstract String performGcmInteraction() throws IOException;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = performGcmInteraction();
                notifyListenersOnSuccess(result);
            } catch (IOException ex) {
                notifyListenersOnError(ex);
            }

            return null;
        }

        private void notifyListenersOnError(IOException ex) {
            GcmProvider provider = getProvider();
            if (provider != null) {
                String errorMessage = "Class: " + ex.getClass() + " Error :" + ex.getMessage();
                //todo check for ex.getMessage().equals("SERVICE_NOT_AVAILABLE" ???
                provider.notifyWorkflowListeners(GcmIntentService.ACTION_ERROR_SERVICE_NOT_AVAILABLE, errorMessage);
            }
        }
    }
}
