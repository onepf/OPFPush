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

package org.onepf.openpush.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.*;
import org.onepf.openpush.Error;
import org.onepf.openpush.gcm.GCMProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class PushSampleActivity extends Activity {

    public static final String GCM_SENDER_ID = "76325631570";
    private static final String TAG = "PushSampleActivity";

    @InjectView(R.id.registration_id)
    TextView mRegistrationIdView;

    @InjectView(R.id.push_provider_name)
    TextView mProviderNameView;

    @InjectView(R.id.register_switch)
    Button mRegisterSwitchView;

    @Optional
    @InjectView(R.id.btn_copy_to_clipboard)
    Button mCopyToClipboardView;

    private static OpenPushHelper mOpenPushHelper;
    private PushSampleActivity.OpenPushEventReceiver mEventReceiver
            = new PushSampleActivity.OpenPushEventReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (mOpenPushHelper == null) {
            mOpenPushHelper = OpenPushHelper.getInstance(PushSampleActivity.this);
            mOpenPushHelper.setListener(mEventReceiver);
            Options.Builder builder = new Options.Builder();
            builder.addProviders(new GCMProvider(this, GCM_SENDER_ID));
            mOpenPushHelper.init(builder.build());
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mOpenPushHelper.isRegistered()) {
            switchToRegisteredState();
        } else {
            switchToUnregisteredState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOpenPushHelper.setListener(mEventReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOpenPushHelper.setListener(null);
    }

    @OnClick(R.id.register_switch)
    void onRegisterClick() {
        if (mOpenPushHelper.isRegistered()) {
            mOpenPushHelper.unregister();
            mRegisterSwitchView.setText(R.string.unregister_in_progress);
            mRegisterSwitchView.setEnabled(false);
        } else {
            mOpenPushHelper.register();
            mRegisterSwitchView.setText(R.string.register_in_progress);
            mRegisterSwitchView.setEnabled(false);
        }
    }

    @Optional
    @OnClick(R.id.btn_copy_to_clipboard)
    void setBtnCopyToClipboard() {
        Toast.makeText(PushSampleActivity.this,
                PushSampleActivity.this.getString(R.string.toast_registration_id_copied),
                Toast.LENGTH_LONG)
                .show();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                ClipData.newPlainText("Push registration token", mRegistrationIdView.getText())
        );
    }

    private void switchToRegisteredState() {
        PushProvider provider = mOpenPushHelper.getCurrentProvider();
        String registrationId = provider == null ? "null" : String.valueOf(provider.getRegistrationId());
        mRegistrationIdView.setText(Html.fromHtml(getString(R.string.registration_id_text, registrationId)));

        String name = provider == null ? "null" : provider.getName();
        mProviderNameView.setText(Html.fromHtml(getString(R.string.push_provider_text, name)));

        mRegisterSwitchView.setText(Html.fromHtml(getString(R.string.unregister)));
        mRegisterSwitchView.setEnabled(true);
        mCopyToClipboardView.setVisibility(View.VISIBLE);
    }

    private void switchToUnregisteredState() {
        mRegistrationIdView.setText(null);
        mProviderNameView.setText(Html.fromHtml(getString(R.string.push_provider_text, "None")));
        mRegisterSwitchView.setText(Html.fromHtml(getString(R.string.register)));
        mRegisterSwitchView.setEnabled(true);
        mCopyToClipboardView.setVisibility(View.GONE);
    }

    public class OpenPushEventReceiver implements OpenPushListener {

        public OpenPushEventReceiver() {
        }

        @Override
        public void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
            Log.i(TAG, String.format("onRegistered(providerName = %s, registrationId = %s)"
                    , providerName, registrationId));
            switchToRegisteredState();

            // You start the registration process by calling register().
            // When the registration ID is ready, OpenPushHelper calls onRegistered() on
            // your app. Transmit the passed-in registration ID to your server, so your
            // server can send messages to this app instance. onRegistered() is also
            // called if your registration ID is rotated or changed for any reason; your
            // app should pass the new registration ID to your server if this occurs.
            // Your server needs to be able to handle a registration ID up to 1536 characters
            // in length.

            // The following is an example of sending the registration ID to your
            // server via a header key/value pair over HTTP.
            sendRegistrationDataToServer(providerName, registrationId);
        }

        private void sendRegistrationDataToServer(String providerName, String registrationId) {
        }

        @Override
        public void onUnregistered(@NotNull String providerName, @Nullable String oldRegistrationId) {
            Log.i(TAG, String.format("onUnregistered(providerName = %s, oldRegistrationId = %s)"
                    , providerName, oldRegistrationId));
            switchToUnregisteredState();
        }

        @Override
        public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        }

        @Override
        public void onDeletedMessages(@NotNull String providerName, int messagesCount) {
        }

        @Override
        public void onRegistrationError(@NotNull String providerName, @NotNull Error error) {
            Toast.makeText(PushSampleActivity.this,
                    String.format("Registration error '%s'", error), Toast.LENGTH_LONG).show();
            if (!error.isRecoverable()) {
                switchToUnregisteredState();
            }
        }

        @Override
        public void onUnregistrationError(@NotNull String providerName, @NotNull Error error) {
            Toast.makeText(PushSampleActivity.this,
                    String.format("Unregistration error '%s'", error), Toast.LENGTH_LONG).show();
            if (!error.isRecoverable()) {
                switchToRegisteredState();
            }
        }

        @Override
        public void onNoAvailableProvider() {
        }

        @Override
        public void onProviderBecameUnavailable(@NotNull String providerName) {
        }
    }
}
