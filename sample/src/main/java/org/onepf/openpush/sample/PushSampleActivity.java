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

package org.onepf.openpush.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.BroadcastOpenPushListener;
import org.onepf.openpush.OpenPushBaseReceiver;
import org.onepf.openpush.OpenPushConstants;
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.Options;
import org.onepf.openpush.gcm.GCMProvider;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class PushSampleActivity extends ActionBarActivity {

    private static final String TAG = "PushSampleActivity";
    public static final String GCM_SENDER_ID = "76325631570";

    @InjectView(R.id.label_registration_id)
    TextView tvLabelRegistrationId;

    @InjectView(R.id.tv_registration_id)
    TextView tvRegistrationId;

    @InjectView(R.id.tv_registration_status)
    TextView tvRegistrationStatus;

    @InjectView(R.id.tv_push_provider)
    TextView tvProviderName;

    @InjectView(R.id.btn_register)
    Button btnRegister;

    @InjectView(R.id.btn_copy_to_clipboard)
    Button btnCopyToClipboard;

    private BroadcastReceiver mOpenPushReceiver;
    private static OpenPushHelper mOpenPushHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mOpenPushHelper == null) {
            mOpenPushHelper = OpenPushHelper.getInstance(PushSampleActivity.this);
            Options.Builder builder = new Options.Builder();
            builder.addProvider(new GCMProvider(PushSampleActivity.this, GCM_SENDER_ID));
            mOpenPushHelper.init(builder.build());
            mOpenPushHelper.setListener(new BroadcastOpenPushListener(this));
        }

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (mOpenPushHelper.getInitStatus() == OpenPushHelper.INIT_SUCCESS) {
            switchToRegisteredState(mOpenPushHelper.getCurrentProviderName(),
                    mOpenPushHelper.getCurrentProviderRegistrationId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOpenPushReceiver == null) {
            registerReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenPushReceiver != null) {
            unregisterReceiver(mOpenPushReceiver);
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(OpenPushConstants.ACTION_REGISTERED);
        filter.addAction(OpenPushConstants.ACTION_UNREGISTERED);
        filter.addAction(OpenPushConstants.ACTION_MESSAGE);
        filter.addAction(OpenPushConstants.ACTION_REGISTRATION_ERROR);
        filter.addAction(OpenPushConstants.ACTION_NO_AVAILABLE_PROVIDER);
        filter.addAction(OpenPushConstants.ACTION_DELETED_MESSAGES);
        filter.addAction(OpenPushConstants.ACTION_HOST_APP_REMOVED);
        mOpenPushReceiver = new OpenPushEventReceiver();
        registerReceiver(mOpenPushReceiver, filter);
    }

    @OnClick(R.id.btn_register)
    void onRegisterClick() {
        if (mOpenPushHelper.getInitStatus() == OpenPushHelper.INIT_SUCCESS) {
            mOpenPushHelper.unregister();
        } else if (mOpenPushHelper.getInitStatus()
                == OpenPushHelper.INIT_NOT_STARTED) {
            mOpenPushHelper.register();
        }
    }

    @OnClick(R.id.btn_copy_to_clipboard)
    void setBtnCopyToClipboard() {
        Toast.makeText(PushSampleActivity.this,
                PushSampleActivity.this.getString(R.string.toast_registration_id_copied),
                Toast.LENGTH_LONG)
                .show();

        ClipboardManager clipboard
                = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
        clipboard.setText(tvRegistrationId.getText());

        Log.i(TAG, "Registration id: " + tvRegistrationId.getText());
    }

    private void switchToRegisteredState(String providerName, String registrationId) {
        tvRegistrationId.setVisibility(View.VISIBLE);
        tvRegistrationId.setText(providerName);

        tvLabelRegistrationId.setVisibility(View.VISIBLE);

        tvProviderName.setText(registrationId);

        btnRegister.setText(R.string.btn_unregister);

        btnCopyToClipboard.setVisibility(View.VISIBLE);
        tvRegistrationStatus.setText(getString(R.string.registered));
    }

    private void switchToUnregisteredState() {
        mOpenPushReceiver = null;

        tvRegistrationId.setVisibility(View.GONE);
        tvRegistrationId.setText(null);

        tvLabelRegistrationId.setVisibility(View.GONE);

        tvProviderName.setText("None");

        btnRegister.setText(R.string.btn_register);

        btnCopyToClipboard.setVisibility(View.GONE);
        tvRegistrationStatus.setText(getString(R.string.not_registered));
    }

    public class OpenPushEventReceiver extends OpenPushBaseReceiver {

        public OpenPushEventReceiver() {
        }

        @Override
        public void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
            switchToRegisteredState(providerName, registrationId);
        }

        @Override
        public void onUnregistered(@NotNull String providerName, @Nullable String registrationId) {
            switchToUnregisteredState();
        }
    }
}
