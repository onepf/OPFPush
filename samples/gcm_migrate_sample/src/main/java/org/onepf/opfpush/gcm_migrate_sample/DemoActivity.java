/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onepf.opfpush.gcm_migrate_sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.onepf.opfpush.Error;
import org.onepf.opfpush.OPFPushHelper;

/**
 * Main UI for the demo app.
 */
public class DemoActivity extends Activity {

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demo";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    TextView mDisplay;
    OPFPushHelper mPushHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mDisplay = (TextView) findViewById(R.id.display);
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplay.setText("");
            }
        });


        mPushHelper = OPFPushHelper.getInstance(this);
        mPushHelper.setListener(new EventListener());
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            if (!mPushHelper.isRegistered()) {
                mPushHelper.register();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public final class EventListener implements org.onepf.opfpush.EventListener {

        public EventListener() {
        }

        @Override
        public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
            Log.i(TAG, String.format("onRegistered(providerName = %s, registrationId = %s)"
                    , providerName, registrationId));
            mDisplay.append("Device registered, registration ID=");
            mDisplay.append(registrationId);
            sendRegistrationDataToServer(providerName, registrationId);
        }

        /**
         * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
         * messages to your app. Not needed for this demo since the device sends upstream messages
         * to a server that echoes back the message using the 'from' address in the message.
         */
        private void sendRegistrationDataToServer(String providerName, String registrationId) {
        }

        @Override
        public void onUnregistered(@NonNull String providerName, @Nullable String oldRegistrationId) {
            mDisplay.append("Device unregistered, old registration ID = ." + oldRegistrationId + "\n");
        }

        @Override
        public void onRegistrationError(@NonNull String providerName, @NonNull Error error) {
            mDisplay.append("Registration error: " + error + "\n");
        }

        @Override
        public void onUnregistrationError(@NonNull String providerName, @NonNull Error error) {
            mDisplay.append("Unregistration error: " + error + "\n");
        }

        @Override
        public void onNoAvailableProvider() {
            mDisplay.append("No available providers.\n");
        }

        @Override
        public void onProviderBecameUnavailable(@NonNull String providerName) {
            mDisplay.append("Provider unavailable.\n");
        }
    }
}
