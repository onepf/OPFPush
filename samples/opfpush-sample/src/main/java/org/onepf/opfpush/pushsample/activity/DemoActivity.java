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

package org.onepf.opfpush.pushsample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.pushsample.R;
import org.onepf.opfpush.pushsample.model.MessageEvent;
import org.onepf.opfpush.pushsample.model.RegisteredEvent;
import org.onepf.opfpush.pushsample.model.UnregisteredEvent;

import de.greenrobot.event.EventBus;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class DemoActivity extends Activity {

    @NonNull
    private TextView registrationIdTextView;

    @NonNull
    private Button registerButton;

    @NonNull
    private Button unregisterButton;

    @NonNull
    private ArrayAdapter<String> adapter;

    @NonNull
    private OPFPushHelper helper = OPFPushHelper.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        registrationIdTextView = (TextView) findViewById(R.id.registration_id);

        registerButton = (Button) findViewById(R.id.register_button);
        unregisterButton = (Button) findViewById(R.id.unregister_button);

        final String registrationId = helper.getRegistrationId();
        if (!TextUtils.isEmpty(registrationId)) {
            OPFPushLog.d("Registration Id : " + registrationId);
            initViewsRegisteredState(registrationId);
        } else {
            initViewsRegisteringState();
        }

        adapter = new ArrayAdapter<>(this, R.layout.item_message);

        final ListView messagesListView = (ListView) findViewById(R.id.messages_list);
        messagesListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onRegisterClick(@NonNull final View view) {
        if (helper.isRegistrationAvailable()) {
            initViewsRegisteringState();
            helper.register();
        } else {
            Toast.makeText(this, R.string.registration_not_available, Toast.LENGTH_SHORT).show();
            initViewsUnregisteringState();
        }
    }

    public void onUnregisterClick(@NonNull final View view) {
        if (helper.isUnregistrationAvailable()) {
            initViewsUnregisteringState();
            helper.unregister();
        } else {
            Toast.makeText(this, R.string.unregistration_not_available, Toast.LENGTH_SHORT).show();
            initViewsRegisteringState();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(@NonNull final MessageEvent messageEvent) {
        final String messageString = messageEvent.getMessage();
        if (!TextUtils.isEmpty(messageString)) {
            adapter.add(messageString);
        }
        EventBus.getDefault().removeStickyEvent(messageEvent);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(@NonNull final RegisteredEvent registeredEvent) {
        final String registrationIdString = registeredEvent.getRegistrationId();
        if (!TextUtils.isEmpty(registrationIdString)) {
            initViewsRegisteredState(registrationIdString);
        }
        EventBus.getDefault().removeStickyEvent(registeredEvent);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(@NonNull final UnregisteredEvent unregisteredEvent) {
        final String registrationIdString = unregisteredEvent.getRegistrationId();
        if (!TextUtils.isEmpty(registrationIdString)) {
            initViewsUnregisteredState(registrationIdString);
        }
    }

    private void initViewsRegisteringState() {
        registrationIdTextView.setText(getString(R.string.registration));
        registerButton.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        unregisterButton.setVisibility(View.GONE);
    }

    private void initViewsRegisteredState(@NonNull final String registrationId) {
        registrationIdTextView.setText(getString(R.string.registered_state_fmt, registrationId));
        registerButton.setVisibility(View.GONE);
        unregisterButton.setVisibility(View.VISIBLE);
        unregisterButton.setEnabled(true);
    }

    private void initViewsUnregisteredState(@NonNull final String registrationId) {
        registrationIdTextView.setText(getString(R.string.unregistered_state_fmt, registrationId));
        registerButton.setVisibility(View.VISIBLE);
        registerButton.setEnabled(true);
        unregisterButton.setVisibility(View.GONE);
    }

    private void initViewsUnregisteringState() {
        registrationIdTextView.setText(getString(R.string.unregistration));
        registerButton.setVisibility(View.GONE);
        unregisterButton.setVisibility(View.VISIBLE);
        unregisterButton.setEnabled(false);
    }
}
