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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
public class AlertDialogFragment extends DialogFragment {

    public static final String TAG = AlertDialogFragment.class.getName();

    private static final String MESSAGE_BUNDLE_KEY = "MESSAGE_BUNDLE_KEY";

    private OnClickListener onClickListener;

    public static AlertDialogFragment newInstance(@NonNull final String message) {
        final AlertDialogFragment dialogFragment = new AlertDialogFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(MESSAGE_BUNDLE_KEY, message);
        dialogFragment.setArguments(arguments);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder.setMessage(getArguments().getString(MESSAGE_BUNDLE_KEY));

        if (onClickListener == null) {
            onClickListener = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            };
        }

        dialogBuilder.setPositiveButton(android.R.string.ok, onClickListener);

        return dialogBuilder.create();
    }

    public void setOnClickListener(@NonNull final OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
