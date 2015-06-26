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

package org.onepf.pushchat.ui.fragment.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.gcm.GCMPubSubHelper;
import org.onepf.pushchat.R;
import org.onepf.pushchat.controller.StateController;
import org.onepf.pushchat.model.PushState;

import static android.widget.Toast.LENGTH_SHORT;
import static java.util.Locale.US;
import static org.onepf.pushchat.model.PushState.NO_AVAILABLE_PROVIDER;
import static org.onepf.pushchat.model.PushState.REGISTERED;
import static org.onepf.pushchat.utils.Constants.PROVIDER_NAME_EXTRA_KEY;
import static org.onepf.pushchat.utils.Constants.REGISTERED_ACTION;
import static org.onepf.pushchat.utils.Constants.REGISTRATION_ID_EXTRA_KEY;
import static org.onepf.pushchat.utils.Constants.UNREGISTERED_ACTION;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class StateFragment extends BaseContentFragment {

    public static final int POSITION = 0;

    private TextView stateTextView;

    private TextView providerNameTextView;

    private TextView registrationIdTextView;

    private Button registerButton;

    private EditText topicEditText;

    private Button subscribeButton;

    private Button unsubscribeButton;

    @Nullable
    private BroadcastReceiver updateStateReceiver;

    @NonNull
    public static StateFragment newInstance() {
        return new StateFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_state, container, false);

        final TextView uuidTextView = (TextView) view.findViewById(R.id.uuid_text);
        uuidTextView.setText(getString(R.string.uuid_fmt, getPushChatApplication().getUUID()));

        stateTextView = (TextView) view.findViewById(R.id.state_text);
        providerNameTextView = (TextView) view.findViewById(R.id.provider_name_text);
        registrationIdTextView = (TextView) view.findViewById(R.id.registration_id_text);
        registerButton = (Button) view.findViewById(R.id.register_button);
        topicEditText = (EditText) view.findViewById(R.id.topic_input);
        subscribeButton = (Button) view.findViewById(R.id.subscribe_topic_button);
        unsubscribeButton = (Button) view.findViewById(R.id.unsubscribe_topic_button);

        registerButton.setOnClickListener(onRegistrationClickListener());
        subscribeButton.setOnClickListener(onSubscribeClickListener());
        unsubscribeButton.setOnClickListener(onUnsubscribeClickListener());

        topicEditText.addTextChangedListener(new TopicTextWatcher());

        registerReceiver();
        initState();

        return view;
    }

    @SuppressWarnings("AssignmentToNull")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
        hideShareButton();
        stateTextView = null;
        providerNameTextView = null;
        registrationIdTextView = null;
        registerButton = null;
        topicEditText = null;
        subscribeButton = null;
        unsubscribeButton = null;
    }

    @Override
    public int getTitleResId() {
        return R.string.title_state_fragment;
    }

    @Override
    public int getPosition() {
        return POSITION;
    }

    private void initState() {
        final OPFPushHelper helper = OPFPush.getHelper();
        final PushState state = StateController.getState(getActivity());
        switch (state) {
            case REGISTERED:
                //noinspection ConstantConditions
                initRegisteredState(helper.getProviderName(), helper.getRegistrationId());
                break;
            case REGISTERING:
                initRegisteringState();
                break;
            case UNREGISTERED:
                initUnregisteredState(false);
                break;
            case UNREGISTERING:
                initUnregisteringState();
                break;
            case NO_AVAILABLE_PROVIDER:
                initUnregisteredState(true);
                break;
        }
    }

    private void initRegisteringState() {
        hideShareButton();
        showProgressBar();

        stateTextView.setText(getString(R.string.state_fmt, getString(R.string.registering)));
        providerNameTextView.setVisibility(View.GONE);
        registrationIdTextView.setVisibility(View.GONE);
        registerButton.setText(getString(R.string.register_button_text));
        registerButton.setEnabled(false);
        subscribeButton.setEnabled(false);
        unsubscribeButton.setEnabled(false);
    }

    private void initRegisteredState(@NonNull final String providerName,
                                     @NonNull final String registrationId) {
        hideProgressBar();
        showShareButton();

        stateTextView.setText(getString(R.string.state_fmt, getString(R.string.registered)));
        providerNameTextView.setText(getString(R.string.provider_name_fmt, providerName));
        registrationIdTextView.setText(getString(R.string.registration_id_fmt, registrationId));
        providerNameTextView.setVisibility(View.VISIBLE);
        registrationIdTextView.setVisibility(View.VISIBLE);
        registerButton.setText(getString(R.string.unregister_button_text));
        registerButton.setEnabled(true);
        subscribeButton.setEnabled(!TextUtils.isEmpty(topicEditText.getText()));
        unsubscribeButton.setEnabled(!TextUtils.isEmpty(topicEditText.getText()));
    }

    private void initUnregisteredState(final boolean isNoAvailableProvider) {
        hideProgressBar();
        hideShareButton();

        stateTextView.setText(getString(R.string.state_fmt, isNoAvailableProvider
                        ? getString(R.string.no_available_provider)
                        : getString(R.string.unregistered))
        );
        providerNameTextView.setVisibility(View.GONE);
        registrationIdTextView.setVisibility(View.GONE);
        registerButton.setText(getString(R.string.register_button_text));
        registerButton.setEnabled(true);
        subscribeButton.setEnabled(false);
        unsubscribeButton.setEnabled(false);
    }

    private void initUnregisteringState() {
        hideShareButton();
        showProgressBar();

        stateTextView.setText(getString(R.string.state_fmt, getString(R.string.unregistering)));
        providerNameTextView.setVisibility(View.GONE);
        registrationIdTextView.setVisibility(View.GONE);
        registerButton.setText(getString(R.string.unregister_button_text));
        registerButton.setEnabled(false);
        subscribeButton.setEnabled(false);
        unsubscribeButton.setEnabled(false);
    }

    private void registerReceiver() {
        if (updateStateReceiver == null) {
            updateStateReceiver = new UpdateStateReceiver();
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(REGISTERED_ACTION);
            intentFilter.addAction(UNREGISTERED_ACTION);
            getActivity().registerReceiver(updateStateReceiver, intentFilter);
        }
    }

    private void unregisterReceiver() {
        if (updateStateReceiver != null) {
            getActivity().unregisterReceiver(updateStateReceiver);
            updateStateReceiver = null;
        }
    }

    private class UpdateStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case REGISTERED_ACTION:
                    initRegisteredState(
                            intent.getStringExtra(PROVIDER_NAME_EXTRA_KEY),
                            intent.getStringExtra(REGISTRATION_ID_EXTRA_KEY)
                    );
                    break;
                case UNREGISTERED_ACTION:
                    initUnregisteredState(StateController.getState(context) == NO_AVAILABLE_PROVIDER);
                    break;
            }
        }
    }

    private View.OnClickListener onRegistrationClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PushState state = StateController.getState(getActivity());

                switch (state) {
                    case REGISTERED:
                        initUnregisteringState();
                        OPFPush.getHelper().unregister();
                        break;
                    case NO_AVAILABLE_PROVIDER:
                    case UNREGISTERED:
                        initRegisteringState();
                        OPFPush.getHelper().register();
                        break;
                }
            }
        };
    }

    private View.OnClickListener onSubscribeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getActivity();
                final String topic = getString(R.string.topic_fmt, topicEditText.getText().toString());
                topicEditText.setText("");
                GCMPubSubHelper.getInstance(context).subscribe(
                        topic,
                        null,
                        new PubSubCallback(
                                String.format(
                                        US,
                                        "Subscription to topic %s has been performed successfully.",
                                        topic
                                ),
                                String.format(
                                        US,
                                        "Subscription to topic %s hasn't been performed.",
                                        topic
                                )
                        )
                );
            }
        };
    }

    private View.OnClickListener onUnsubscribeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getActivity();
                final String topic = getString(R.string.topic_fmt, topicEditText.getText().toString());
                topicEditText.setText("");
                GCMPubSubHelper.getInstance(context).unsubscribe(
                        topic,
                        new PubSubCallback(
                                String.format(
                                        US,
                                        "Unsubscribtion from topic %s has been performed successfully.",
                                        topic
                                ),
                                String.format(
                                        US,
                                        "Unsubscribtion from topic %s hasn't been performed.",
                                        topic
                                )
                        )
                );
            }
        };
    }

    private final class PubSubCallback implements GCMPubSubHelper.Callback {

        @NonNull
        private final String successString;

        @NonNull
        private final String errorString;

        public PubSubCallback(@NonNull final String successString,
                              @NonNull final String errorString) {
            this.successString = successString;
            this.errorString = errorString;
        }

        @Override
        public void onSuccess() {
            Toast.makeText(getActivity(), successString, LENGTH_SHORT).show();
        }

        @Override
        public void onError(@Nullable final String error) {
            Toast.makeText(
                    getActivity(),
                    String.format(US, "%1$s Error message : %2$s", errorString, error),
                    LENGTH_SHORT
            ).show();
        }
    }

    private final class TopicTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(final Editable s) {
            final boolean isEnabled = !s.toString().isEmpty() && StateController.getState(getActivity()) == REGISTERED;
            subscribeButton.setEnabled(isEnabled);
            unsubscribeButton.setEnabled(isEnabled);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //nothing
        }
    }
}
