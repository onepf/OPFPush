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
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.Options;
import org.onepf.openpush.gcm.GCMProvider;
import org.onepf.openpush.sample.event.RegisteredEvent;
import org.onepf.openpush.sample.event.UnregisteredEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import de.greenrobot.event.EventBus;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class PushSampleActivity extends Activity {

    private static final String WEB_SERVER_URL = "http://localhost:8080";
    public static final String GCM_SENDER_ID = "76325631570";
    private static final String TAG = "PushSampleActivity";

    @InjectView(R.id.registration_id)
    TextView mRegistrationIdView;

    @InjectView(R.id.registration_status)
    TextView mRegistrationStatusView;

    @InjectView(R.id.push_provider_name)
    TextView mProviderNameView;

    @InjectView(R.id.register_switch)
    Button mRegisterSwitchView;

    @Optional
    @InjectView(R.id.btn_copy_to_clipboard)
    Button mCopyToClipboardView;

    private boolean mRegistered;
    private static OpenPushHelper mOpenPushHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mOpenPushHelper == null) {
            mOpenPushHelper = OpenPushHelper.getInstance(PushSampleActivity.this);
            mOpenPushHelper.setListener(new EventBusListener());
            Options.Builder builder = new Options.Builder();
            builder.addProviders(new GCMProvider(PushSampleActivity.this, GCM_SENDER_ID));
            mOpenPushHelper.init(builder.build());
        }

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (mOpenPushHelper.getState() == OpenPushHelper.STATE_RUNNING) {
            mRegistered = true;
            switchToRegisteredState(mOpenPushHelper.getCurrentProviderName(),
                    mOpenPushHelper.getCurrentProviderRegistrationId());
        } else {
            switchToUnregisteredState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRegistered) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.register_switch)
    void onRegisterClick() {
        if (mOpenPushHelper.getState() == OpenPushHelper.STATE_RUNNING) {
            mOpenPushHelper.unregister();
        } else if (mOpenPushHelper.getState()
                == OpenPushHelper.STATE_NONE) {
            mRegistered = true;
            EventBus.getDefault().register(this);
            mOpenPushHelper.register();
        }
    }

    public void onEventMainThread(UnregisteredEvent e) {
        Log.i(TAG, String.format("onUnregistered(providerName = %s, oldRegistrationId = %s)"
                , e.getProviderName(), e.getOldRegistrationId()));
        switchToUnregisteredState();
    }

    public void onEventMainThread(RegisteredEvent e) {
        Log.i(TAG, String.format("onRegistered(providerName = %s, registrationId = %s)"
                , e.getProviderName(), e.getRegistrationId()));
        switchToRegisteredState(e.getProviderName(), e.getRegistrationId());

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
        sendRegistrationDataToServer(e.getProviderName(), e.getRegistrationId());
    }

    private static void sendRegistrationDataToServer(String providerName, String registrationId) {
        try {
            URL url = new URL(WEB_SERVER_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("RegistrationId", registrationId);
            con.setRequestProperty("ProviderName", providerName);
            con.getResponseCode();
        } catch (IOException e) {
            Log.e(TAG, "Can't send registration data to server.", e);
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

    private void switchToRegisteredState(String providerName, String registrationId) {
        mRegistrationIdView.setText(Html.fromHtml(getString(R.string.registration_id_text, registrationId)));
        mProviderNameView.setText(Html.fromHtml(getString(R.string.push_provider_text, providerName)));
        mRegisterSwitchView.setText(Html.fromHtml(getString(R.string.unregister)));
        mRegistrationStatusView.setText(Html.fromHtml(getString(R.string.registered_status)));
        mCopyToClipboardView.setVisibility(View.VISIBLE);
    }

    private void switchToUnregisteredState() {
        mRegistered = false;
        mRegistrationIdView.setText(null);
        mProviderNameView.setText(Html.fromHtml(getString(R.string.push_provider_text, "None")));
        mRegisterSwitchView.setText(Html.fromHtml(getString(R.string.register)));
        mRegistrationStatusView.setText(Html.fromHtml(getString(R.string.unregistered_status)));
        mCopyToClipboardView.setVisibility(View.GONE);
    }
}
