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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.pushsample.BusProvider;
import org.onepf.opfpush.pushsample.R;
import org.onepf.opfpush.pushsample.model.Message;
import org.onepf.opfpush.pushsample.model.RegistrationId;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class DemoActivity extends Activity {

    @NonNull
    private TextView registrationIdTextView;

    @NonNull
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        registrationIdTextView = (TextView) findViewById(R.id.registration_id);

        final OPFPushHelper helper = OPFPushHelper.getInstance(this);
        final String registrationId = helper.getRegistrationId();
        if (!TextUtils.isEmpty(registrationId)) {
            registrationIdTextView.setText(getString(R.string.registration_id_fmt, registrationId));
        } else {
            registrationIdTextView.setText(R.string.registration);
        }

        adapter = new ArrayAdapter<>(this, R.layout.item_message);

        final ListView messagesListView = (ListView) findViewById(R.id.messages_list);
        messagesListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @SuppressWarnings("UnusedDeclaration")
    @Subscribe
    public void onMessageReceive(@NonNull final Message message) {
        final String messageString = message.getMessage();
        if (!TextUtils.isEmpty(messageString)) {
            adapter.add(messageString);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @Subscribe
    public void onRegistered(@NonNull final RegistrationId registrationId) {
        final String registrationIdString = registrationId.getRegistrationId();
        if (!TextUtils.isEmpty(registrationIdString)) {
            registrationIdTextView.setText(getString(R.string.registration_id_fmt,
                    registrationIdString));
        }
    }
}
