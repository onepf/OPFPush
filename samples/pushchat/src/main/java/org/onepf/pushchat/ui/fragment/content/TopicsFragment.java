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

import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.gcm.GCMConstants;
import org.onepf.opfpush.gcm.GCMPubSubHelper;
import org.onepf.opfutils.OPFLog;
import org.onepf.pushchat.R;
import org.onepf.pushchat.controller.StateController;
import org.onepf.pushchat.model.response.TopicsResponse;
import org.onepf.pushchat.retrofit.NetworkController;
import org.onepf.pushchat.ui.dialog.UnsubscribeDialogFragment;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.onepf.pushchat.model.PushState.REGISTERED;
import static org.onepf.pushchat.utils.Constants.REGISTERED_ACTION;
import static org.onepf.pushchat.utils.Constants.UNREGISTERED_ACTION;

/**
 * @author Roman Savin
 * @since 02.07.2015
 */
public class TopicsFragment extends BaseContentFragment {

    public static final int POSITION = 1;

    private TextView infoText;

    private EditText topicEditText;

    private Button subscribeButton;

    private ListView topicsListView;

    private ProgressBar progressBar;

    private ArrayAdapter<String> adapter;

    @Nullable
    private BroadcastReceiver receiver;

    public static TopicsFragment newInstance() {
        return new TopicsFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        adapter = new ArrayAdapter<>(getActivity(), R.layout.item_topic);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_topics, container, false);

        infoText = (TextView) view.findViewById(R.id.topic_info);
        topicEditText = (EditText) view.findViewById(R.id.topic_input);
        subscribeButton = (Button) view.findViewById(R.id.subscribe_topic_button);
        topicsListView = (ListView) view.findViewById(R.id.topics_list);
        progressBar = (ProgressBar) view.findViewById(R.id.topic_progress_bar);

        subscribeButton.setOnClickListener(new OnSubscribeClickListener());
        topicEditText.addTextChangedListener(new TopicTextWatcher());
        topicsListView.setAdapter(adapter);
        topicsListView.setOnItemLongClickListener(new OnTopicItemLongLickListener());

        registerReceiver();
        initViews();

        return view;
    }

    @SuppressWarnings("AssignmentToNull")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
        infoText = null;
        topicEditText = null;
        subscribeButton = null;
        topicsListView = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard(getView());
    }

    @SuppressWarnings("AssignmentToNull")
    @Override
    public void onDetach() {
        super.onDetach();
        adapter = null;
    }

    @Override
    public int getTitleResId() {
        return R.string.title_topics_fragment;
    }

    @Override
    public int getPosition() {
        return POSITION;
    }

    private void initViews() {
        final String currentProviderName = OPFPush.getHelper().getProviderName();

        if (TextUtils.isEmpty(currentProviderName)) {
            initTopicsUnavailableVisibility(getString(R.string.no_available_provider));
        } else if (!GCMConstants.PROVIDER_NAME.equals(currentProviderName)) {
            initTopicsUnavailableVisibility(getString(R.string.provider_not_support_topics, currentProviderName));
        } else {
            initTopicsAvailableVisibility();
            loadTopics();
        }
    }

    private void initTopicsUnavailableVisibility(@NonNull final String reason) {
        infoText.setVisibility(VISIBLE);
        infoText.setText(reason);

        topicEditText.setVisibility(GONE);
        subscribeButton.setVisibility(GONE);
        topicsListView.setVisibility(GONE);
    }

    private void initTopicsAvailableVisibility() {
        infoText.setVisibility(GONE);
        topicEditText.setVisibility(VISIBLE);
        subscribeButton.setVisibility(VISIBLE);
        topicsListView.setVisibility(VISIBLE);
    }

    private void loadTopics() {
        safeSetProgressBarVisibility(VISIBLE);
        NetworkController.getInstance().getTopics(getActivity(), new LoadTopicsCallback());
    }

    private void safeSetProgressBarVisibility(final int visibility) {
        if (progressBar != null) {
            progressBar.setVisibility(visibility);
        }
    }

    private void registerReceiver() {
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    initViews();
                }
            };

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(REGISTERED_ACTION);
            intentFilter.addAction(UNREGISTERED_ACTION);
            getActivity().registerReceiver(receiver, intentFilter);
        }
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private final class TopicTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(final Editable s) {
            final boolean isEnabled = !s.toString().isEmpty() && StateController.getState(getActivity()) == REGISTERED;
            subscribeButton.setEnabled(isEnabled);
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

    private final class OnTopicItemLongLickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            if (adapter == null) {
                return false;
            }
            final String topic = adapter.getItem(position);
            final UnsubscribeDialogFragment unsubscribeDialogFragment = UnsubscribeDialogFragment.newInstance(topic);
            unsubscribeDialogFragment.setOnUnsubscribeCallback(new UnsubscribeDialogFragment.OnUnsubscribeCallback() {
                @Override
                public void onUnsubscribe() {
                    adapter.remove(topic);
                }
            });
            unsubscribeDialogFragment.show(getFragmentManager(), UnsubscribeDialogFragment.TAG);
            return true;
        }
    }

    private final class OnSubscribeClickListener implements View.OnClickListener {

        @Override
        public void onClick(@NonNull final View v) {
            final Context context = getActivity();
            if (topicEditText == null || context == null) {
                return;
            }

            final String topic = context.getString(R.string.topic_fmt, topicEditText.getText().toString());
            topicEditText.setText("");
            GCMPubSubHelper.getInstance(context).subscribe(topic, null, new OnGcmSubscribeCallback(topic));
        }
    }

    private final class OnGcmSubscribeCallback implements GCMPubSubHelper.Callback {

        @NonNull
        private final String topic;

        public OnGcmSubscribeCallback(@NonNull final String topic) {
            this.topic = topic;
        }

        @Override
        public void onSuccess() {
            OPFLog.logMethod();
            final Context context = getActivity();
            if (context == null) {
                return;
            }

            NetworkController.getInstance().subscribe(context, topic, new OnServerSubscribeCallback(topic));
        }

        @Override
        public void onError(@Nullable final String error) {
            OPFLog.logMethod(error);
            final Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, error, LENGTH_SHORT).show();
            }
        }
    }

    private final class OnServerSubscribeCallback implements Callback<Object> {

        @NonNull
        private final String topic;

        public OnServerSubscribeCallback(@NonNull final String topic) {
            this.topic = topic;
        }

        @Override
        public void success(final Object o, final Response response) {
            OPFLog.logMethod(o, response);
            //Add topic to adapter if it's not already added.
            if (adapter != null && topicsListView != null && adapter.getPosition(topic) < 0) {
                adapter.add(topic);
            }
        }

        @Override
        public void failure(final RetrofitError error) {
            OPFLog.logMethod(error);
            final Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, error.getMessage(), LENGTH_SHORT).show();
            }
        }
    }

    private final class LoadTopicsCallback implements Callback<TopicsResponse> {

        @Override
        public void success(@NonNull final TopicsResponse topicsResponse, @NonNull final Response response) {
            OPFLog.logMethod(topicsResponse, response);
            safeSetProgressBarVisibility(GONE);
            initTopicsList(topicsResponse.topics);
        }

        @Override
        public void failure(@NonNull final RetrofitError error) {
            OPFLog.logMethod(error);
            OPFLog.e(error.getMessage());
            safeSetProgressBarVisibility(GONE);
            final Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, error.getMessage(), LENGTH_SHORT).show();
            }
        }

        private void initTopicsList(@Nullable final List<String> topics) {
            if (topicsListView == null || adapter == null || topics == null) {
                return;
            }

            adapter.clear();
            adapter.addAll(topics);
        }
    }
}
