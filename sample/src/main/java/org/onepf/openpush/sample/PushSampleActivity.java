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
import org.onepf.openpush.OpenPushBaseReceiver;
import org.onepf.openpush.OpenPushConstants;
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.Options;
import org.onepf.openpush.gcm.GCMProvider;
import org.onepf.openpush.nokia.NokiaPushProvider;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class PushSampleActivity extends ActionBarActivity {

    private static final String TAG = "PushSampleActivity";
    public static final String GCM_SENDER_ID = "76325631570";

    private TextView tvLabelRegistrationId;

    private TextView tvRegistrationId;
    private TextView tvRegistrationStatus;

    private TextView tvLabelMessage;
    private TextView tvMessage;

    private TextView tvProviderName;

    private Button btnRegister;
    private Button btnUnregister;
    private Button btnCopyToClipboard;

    private BroadcastReceiver mOpenPushReceiver;
    private OpenPushHelper mOpenPushHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOpenPushHelper = OpenPushHelper.getInstance(PushSampleActivity.this);
        setContentView(R.layout.activity_main);
        initViews();
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

    private void initViews() {
        tvLabelRegistrationId = (TextView) findViewById(R.id.label_registration_id);

        tvRegistrationId = (TextView) findViewById(R.id.tv_registration_id);
        tvRegistrationStatus = (TextView) findViewById(R.id.tv_registration_status);

        tvLabelMessage = (TextView) findViewById(R.id.label_message);
        tvMessage = (TextView) findViewById(R.id.tv_message_content);

        tvProviderName = (TextView) findViewById(R.id.tv_push_provider);

        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOpenPushHelper.getInitStatus()
                        == OpenPushHelper.INIT_NOT_STARTED) {
                    Options.Builder builder = new Options.Builder();
                    builder.addProvider(new GCMProvider(PushSampleActivity.this, GCM_SENDER_ID));
                    mOpenPushHelper.register(builder.build());
                }
            }
        });

        btnUnregister = (Button) findViewById(R.id.btn_unregister);
        btnUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenPushHelper.unregister();
            }
        });

        btnCopyToClipboard = (Button) findViewById(R.id.btn_copy_to_clipboard);
        btnCopyToClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PushSampleActivity.this,
                        PushSampleActivity.this.getString(R.string.toast_registration_id_copied),
                        Toast.LENGTH_LONG)
                        .show();

                ClipboardManager clipboard
                        = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
                clipboard.setText(tvRegistrationId.getText());

                Log.i(TAG, "Registration id: " + tvRegistrationId.getText());
            }
        });
    }

    private void switchToRegisteredState(String providerName, String registrationId) {
        tvRegistrationId.setVisibility(View.VISIBLE);
        tvRegistrationId.setText(providerName);

        tvLabelRegistrationId.setVisibility(View.VISIBLE);

        tvProviderName.setText(registrationId);

        btnRegister.setVisibility(View.INVISIBLE);
        btnUnregister.setVisibility(View.VISIBLE);

        btnCopyToClipboard.setVisibility(View.VISIBLE);
        tvRegistrationStatus.setText(getString(R.string.registered));
    }

    private void switchToUnregisteredState() {
        mOpenPushReceiver = null;

        tvRegistrationId.setVisibility(View.GONE);
        tvRegistrationId.setText(null);

        tvLabelRegistrationId.setVisibility(View.GONE);

        tvProviderName.setText("None");

        btnRegister.setVisibility(View.VISIBLE);
        btnUnregister.setVisibility(View.INVISIBLE);

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
