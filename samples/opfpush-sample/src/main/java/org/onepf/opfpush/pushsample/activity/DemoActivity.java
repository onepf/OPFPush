/*
 * Copyright 2012-2015 One Platform Foundation
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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.gcm.GCMConstants;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfpush.pushsample.R;
import org.onepf.opfpush.pushsample.model.event.FailedRequestEvent;
import org.onepf.opfpush.pushsample.model.event.MessageEvent;
import org.onepf.opfpush.pushsample.model.event.NoAvailableProviderEvent;
import org.onepf.opfpush.pushsample.model.event.RegisteredEvent;
import org.onepf.opfpush.pushsample.model.event.UnregisteredEvent;
import org.onepf.opfpush.pushsample.retrofit.NetworkController;
import org.onepf.opfutils.OPFLog;

import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.onepf.opfpush.model.UnrecoverablePushError.Type.AVAILABILITY_ERROR;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class DemoActivity extends Activity {

    private EditText messageField;

    private TextView infoText;

    private Button registerButton;

    private Button unregisterButton;

    @SuppressWarnings("NullableProblems")
    @NonNull
    private ArrayAdapter<String> adapter;

    @NonNull
    private OPFPushHelper helper = OPFPush.getHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OPFLog.logMethod();
        setContentView(R.layout.activity_demo);

        messageField = (EditText) findViewById(R.id.message_field);
        infoText = (TextView) findViewById(R.id.info_text);

        registerButton = (Button) findViewById(R.id.register_button);
        unregisterButton = (Button) findViewById(R.id.unregister_button);

        final String registrationId = helper.getRegistrationId();
        if (!TextUtils.isEmpty(registrationId)) {
            OPFLog.d("Registration Id : " + registrationId);
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
        OPFLog.logMethod();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OPFLog.logMethod();
        EventBus.getDefault().unregister(this);
    }

    public void onRegisterClick(@NonNull final View view) {
        OPFLog.logMethod();
        initViewsRegisteringState();
        helper.register();
    }

    public void onUnregisterClick(@NonNull final View view) {
        OPFLog.logMethod();
        initViewsUnregisteringState();
        helper.unregister();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(@NonNull final MessageEvent messageEvent) {
        OPFLog.logMethod(messageEvent);
        final String messageString = messageEvent.getMessage();
        if (!TextUtils.isEmpty(messageString)) {
            adapter.add(messageString);
        }
        EventBus.getDefault().removeStickyEvent(messageEvent);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(@NonNull final RegisteredEvent registeredEvent) {
        OPFLog.logMethod(registeredEvent);
        final String registrationIdString = registeredEvent.getRegistrationId();
        if (!TextUtils.isEmpty(registrationIdString)) {
            initViewsRegisteredState(registrationIdString);
        }
        EventBus.getDefault().removeStickyEvent(registeredEvent);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(@NonNull final UnregisteredEvent unregisteredEvent) {
        OPFLog.logMethod(unregisteredEvent);
        final String registrationIdString = unregisteredEvent.getRegistrationId();
        initViewsUnregisteredState(registrationIdString);
        EventBus.getDefault().removeStickyEvent(unregisteredEvent);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(@NonNull final NoAvailableProviderEvent noAvailableProviderEvent) {
        OPFLog.logMethod(noAvailableProviderEvent);
        initViewsUnregisteredState("");
        final Map<String, UnrecoverablePushError> pushErrors = noAvailableProviderEvent.getPushErrors();

        infoText.setText(getString(R.string.no_available_provider));

        //Log errors.
        if (!pushErrors.isEmpty()) {
            for (Map.Entry<String, UnrecoverablePushError> pushErrorEntry : pushErrors.entrySet()) {
                OPFLog.d("Push provider %1$ss is unavailable. Error : %2$s",
                        pushErrorEntry.getKey(), pushErrorEntry.getValue());
            }
        }

        if (pushErrors.containsKey(GCMConstants.PROVIDER_NAME)) {
            final UnrecoverablePushError gcmError = pushErrors.get(GCMConstants.PROVIDER_NAME);
            if (gcmError.getType() == AVAILABILITY_ERROR
                    && gcmError.getAvailabilityErrorCode() != null) {
                final int errorCode = gcmError.getAvailabilityErrorCode();
                if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
                    GooglePlayServicesUtil.showErrorDialogFragment(errorCode, this, 0);
                }
            }
        }

        EventBus.getDefault().removeStickyEvent(noAvailableProviderEvent);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull final FailedRequestEvent failedRequestEvent) {
        OPFLog.logMethod(failedRequestEvent);
        OPFLog.d("Request failed : " + failedRequestEvent.getErrorMessage());
        //todo show message error
    }

    private void initViewsRegisteringState() {
        OPFLog.logMethod();
        infoText.setText(getString(R.string.registration));
        registerButton.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        unregisterButton.setVisibility(View.GONE);
    }

    private void initViewsRegisteredState(@NonNull final String registrationId) {
        OPFLog.logMethod(registrationId);
        infoText.setText(getString(R.string.registered_state_fmt, registrationId));
        registerButton.setVisibility(View.GONE);
        unregisterButton.setVisibility(View.VISIBLE);
        unregisterButton.setEnabled(true);

        messageField.setEnabled(true);
        messageField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                NetworkController.getInstance()
                        .pushMessage(DemoActivity.this, textView.getText().toString());
                textView.setText("");
                return true;
            }
        });
    }

    private void initViewsUnregisteredState(@Nullable final String registrationId) {
        OPFLog.logMethod(registrationId);
        infoText.setText(getString(R.string.unregistered_state_fmt, registrationId));
        registerButton.setVisibility(View.VISIBLE);
        registerButton.setEnabled(true);
        unregisterButton.setVisibility(View.GONE);

        messageField.setEnabled(false);
    }

    private void initViewsUnregisteringState() {
        OPFLog.logMethod();
        infoText.setText(getString(R.string.unregistration));
        registerButton.setVisibility(View.GONE);
        unregisterButton.setVisibility(View.VISIBLE);
        unregisterButton.setEnabled(false);
    }
}
