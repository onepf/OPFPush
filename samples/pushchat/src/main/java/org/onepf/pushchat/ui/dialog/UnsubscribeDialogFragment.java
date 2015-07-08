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

package org.onepf.pushchat.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import org.onepf.opfpush.gcm.GCMPubSubHelper;
import org.onepf.opfutils.OPFLog;
import org.onepf.pushchat.R;
import org.onepf.pushchat.retrofit.NetworkController;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * @author Roman Savin
 * @since 08.07.2015
 */
public class UnsubscribeDialogFragment extends DialogFragment {

    public static final String TAG = UnsubscribeDialogFragment.class.getName();

    private static final String TOPIC_EXTRA_KEY = "TOPIC_EXTRA_KEY";

    @Nullable
    private OnUnsubscribeCallback onUnsubscribeCallback;

    public static UnsubscribeDialogFragment newInstance(@NonNull final String topic) {
        final UnsubscribeDialogFragment dialogFragment = new UnsubscribeDialogFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(TOPIC_EXTRA_KEY, topic);
        dialogFragment.setArguments(arguments);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final String topic = getArguments().getString(TOPIC_EXTRA_KEY);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder.setMessage(getString(R.string.unsubscribe_question_fmt, topic))
                .setPositiveButton(android.R.string.ok, new OnUnsubscribeClickListener(topic))
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return dialogBuilder.create();
    }

    public void setOnUnsubscribeCallback(@NonNull final OnUnsubscribeCallback onUnsubscribeCallback) {
        this.onUnsubscribeCallback = onUnsubscribeCallback;
    }

    public interface OnUnsubscribeCallback {

        void onUnsubscribe();
    }

    private final class OnUnsubscribeClickListener implements DialogInterface.OnClickListener {

        @NonNull
        private final String topic;

        public OnUnsubscribeClickListener(@NonNull final String topic) {
            this.topic = topic;
        }

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            final Context context = getActivity();
            GCMPubSubHelper.getInstance(context).subscribe(topic, null, new OnGcmUnsubscribeCallback(topic));
        }
    }

    private final class OnGcmUnsubscribeCallback implements GCMPubSubHelper.Callback {

        @NonNull
        private final String topic;

        public OnGcmUnsubscribeCallback(@NonNull final String topic) {
            this.topic = topic;
        }

        @Override
        public void onSuccess() {
            OPFLog.logMethod();
            final Context context = getActivity();
            if (context == null) {
                return;
            }

            NetworkController.getInstance().unsubscribe(context, topic, new OnServerUnsubscribeCallback(topic));
        }

        @Override
        public void onError(@Nullable final String error) {
            OPFLog.logMethod(error);
            final Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final class OnServerUnsubscribeCallback implements Callback<Object> {

        @NonNull
        private final String topic;

        public OnServerUnsubscribeCallback(@NonNull final String topic) {
            this.topic = topic;
        }

        @Override
        public void success(final Object o, final Response response) {
            OPFLog.logMethod(o, response);

            if (onUnsubscribeCallback != null) {
                onUnsubscribeCallback.onUnsubscribe();
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
}
