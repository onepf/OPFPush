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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.onepf.pushchat.PushChatApplication;
import org.onepf.pushchat.R;
import org.onepf.pushchat.db.DatabaseHelper;
import org.onepf.pushchat.model.db.Contact;
import org.onepf.pushchat.model.response.ExistResponse;
import org.onepf.pushchat.retrofit.NetworkController;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
public class AddContactDialogFragment extends DialogFragment {

    public static final String TAG = AddContactDialogFragment.class.getName();

    public static AddContactDialogFragment newInstance() {
        return new AddContactDialogFragment();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_contact, null);
        final EditText nameEditText = (EditText) view.findViewById(R.id.contact_name_edit_text);
        final EditText uuidEditText = (EditText) view.findViewById(R.id.uuid_edit_text);
        final TextView errorTextView = (TextView) view.findViewById(R.id.error_text);
        final Button okButton = (Button) view.findViewById(R.id.ok_button);

        uuidEditText.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {
                okButton.setEnabled(!s.toString().isEmpty() && !nameEditText.getText().toString().isEmpty());
            }
        });

        nameEditText.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {
                okButton.setEnabled(!s.toString().isEmpty() && !uuidEditText.getText().toString().isEmpty());
            }
        });

        okButton.setOnClickListener(onClickListener(nameEditText, uuidEditText, errorTextView));

        dialogBuilder.setTitle(getString(R.string.add_contact_title))
                .setView(view);


        return dialogBuilder.create();
    }

    private View.OnClickListener onClickListener(@NonNull final EditText nameEditText,
                                                 @NonNull final EditText uuidEditText,
                                                 @NonNull final TextView errorTextView) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Perform checks before adding contact to DB
                final String name = nameEditText.getText().toString();
                final String uuid = uuidEditText.getText().toString();

                final PushChatApplication application = (PushChatApplication) getActivity().getApplication();
                final String ownUuid = application.getUUID();

                if (ownUuid.equals(uuid)) {
                    errorTextView.setText(R.string.own_uuid_error);
                    errorTextView.setVisibility(View.VISIBLE);
                } else if (DatabaseHelper.getInstance(getActivity()).isUuidAdded(uuid)) {
                    errorTextView.setText(R.string.uuid_already_added);
                    errorTextView.setVisibility(View.VISIBLE);
                } else {
                    //check existing and add to DB
                    errorTextView.setVisibility(View.GONE);
                    NetworkController.getInstance().exist(uuid, existCallback(name, uuid, errorTextView));
                }
            }
        };
    }

    private Callback<ExistResponse> existCallback(@NonNull final String name,
                                                  @NonNull final String uuid,
                                                  @NonNull final TextView errorTextView) {
        return new Callback<ExistResponse>() {
            @Override
            public void success(@NonNull final ExistResponse existResponse,
                                @NonNull final Response response) {
                //Add to DB if exists
                if (existResponse.exist) {
                    DatabaseHelper.getInstance(getActivity()).addContact(new Contact(name, uuid));
                    dismiss();
                } else {
                    errorTextView.setText(R.string.uuid_not_exist);
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(@NonNull final RetrofitError error) {
                errorTextView.setText(R.string.uuid_not_exist);
                errorTextView.setVisibility(View.VISIBLE);
            }
        };
    }

    private static abstract class AfterTextChangedWatcher implements TextWatcher {

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
