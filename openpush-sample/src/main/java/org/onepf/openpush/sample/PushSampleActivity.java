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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.onepf.openpush.OpenPushLog;
import org.onepf.openpush.OpenPushProvider;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class PushSampleActivity extends ActionBarActivity {

    private static final String TAG = PushSampleActivity.class.getSimpleName();

    private TextView tvLabelRegistrationId;

    private TextView tvRegistrationId;
    private TextView tvRegistrationStatus;

    private TextView tvLabelMessage;
    private TextView tvMessage;

    private TextView tvProviderName;

    private Button btnRegister;
    private Button btnUnregister;
    private Button btnCopyToClipboard;

    private BroadcastReceiver mPushReceiver = new SampleBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mPushReceiver.onReceive(this, intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerPushReceiver();

        if (OpenPushProvider.getInstance().isRegistered()) {
            switchToRegisteredState();
        } else {
            switchToUnregisteredState();
        }
    }

    @Override
    protected void onPause() {
        unregisterPushReceiver();
        super.onPause();
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
                OpenPushProvider.getInstance().register();
            }
        });

        btnUnregister = (Button) findViewById(R.id.btn_unregister);
        btnUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPushProvider.getInstance().unregister();
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

    private void registerPushReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimplePushListener.ONEPF_ACTION_REGISTERED);
        filter.addAction(SimplePushListener.ONEPF_ACTION_UNREGISTERED);
        filter.addAction(SimplePushListener.ONEPF_ACTION_MESSAGE);
        filter.addAction(SimplePushListener.ONEPF_ACTION_ERROR);

        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver, filter);
    }

    private void unregisterPushReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPushReceiver);
    }

    private void switchToRegisteredState() {
        tvRegistrationId.setVisibility(View.VISIBLE);
        tvRegistrationId.setText(OpenPushProvider.getInstance().getRegistrationId());

        tvLabelRegistrationId.setVisibility(View.VISIBLE);

        tvProviderName.setText(OpenPushProvider.getInstance().getName());

        btnRegister.setVisibility(View.INVISIBLE);
        btnUnregister.setVisibility(View.VISIBLE);

        btnCopyToClipboard.setVisibility(View.VISIBLE);
        tvRegistrationStatus.setText(getString(R.string.registered));
    }

    private void switchToUnregisteredState() {
        tvRegistrationId.setVisibility(View.GONE);
        tvRegistrationId.setText("");

        tvLabelRegistrationId.setVisibility(View.GONE);

        tvProviderName.setText("None");

        btnRegister.setVisibility(View.VISIBLE);
        btnUnregister.setVisibility(View.INVISIBLE);

        btnCopyToClipboard.setVisibility(View.GONE);
        tvRegistrationStatus.setText(getString(R.string.not_registered));
    }

    private void onMessageObtained(Bundle extras) {
        if (extras != null && !extras.isEmpty()) {
            tvLabelMessage.setVisibility(View.VISIBLE);
            tvMessage.setVisibility(View.VISIBLE);

            tvMessage.setText(OpenPushLog.bundleToString(extras));
            Log.i(TAG, "Message received in app: " + OpenPushLog.bundleToString(extras));
        }
    }

    private class SampleBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "Push received in app: " + OpenPushLog.intentToString(intent));

            String action = intent.getAction();

            if (SimplePushListener.ONEPF_ACTION_REGISTERED.equals(action)) {
                onRegistered();
            } else if (SimplePushListener.ONEPF_ACTION_UNREGISTERED.equals(action)) {
                onUnregistered();
            } else if (SimplePushListener.ONEPF_ACTION_ERROR.equals(action)) {
                onMessage(intent);
            } else if (SimplePushListener.ONEPF_ACTION_MESSAGE.equals(action)) {
                onMessage(intent);
            }
        }

        private void onRegistered() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchToRegisteredState();
                }
            });
        }

        private void onUnregistered() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchToUnregisteredState();
                }
            });
        }

        private void onMessage(Intent intent) {
            final Bundle message = intent.getBundleExtra(SimplePushListener.ONEPF_EXTRA_MESSAGE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onMessageObtained(message);
                }
            });
        }
    }
}
