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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import org.onepf.pushchat.R;
import org.onepf.pushchat.controller.NotificationController;
import org.onepf.pushchat.db.ContentDescriptor.MessagesContract;
import org.onepf.pushchat.db.DatabaseHelper;
import org.onepf.pushchat.model.Message;
import org.onepf.pushchat.retrofit.NetworkController;
import org.onepf.pushchat.ui.adapter.MessagesCursorAdapter;
import org.onepf.pushchat.ui.dialog.AlertDialogFragment;
import org.onepf.pushchat.utils.ContactsProvider;

import static org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry.RECEIVED_TIME;

/**
 * @author Roman Savin
 * @since 28.04.2015
 */
public class MessagesFragment extends BaseContentFragment {

    private MessagesCursorAdapter adapter;

    @NonNull
    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages, container, false);

        final EditText messageEditText = (EditText) view.findViewById(R.id.message_input);
        messageEditText.setOnEditorActionListener(onEditorActionListener());

        showClearButton();
        initMessagesList(view);
        initLoaderManager();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationController.getInstance().setNeedShowNotification(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationController.getInstance().setNeedShowNotification(true);
        hideKeyboard(getView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideClearButton();
    }

    private TextView.OnEditorActionListener onEditorActionListener() {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final Context context = getActivity();
                if (ContactsProvider.getUuids(context).isEmpty()) {
                    final AlertDialogFragment dialogFragment = AlertDialogFragment
                            .newInstance(getString(R.string.empty_contact_list));
                    dialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
                } else {
                    final String messageText = textView.getText().toString();

                    DatabaseHelper.getInstance(context).addMessage(new Message(
                            getString(R.string.sender_you),
                            messageText,
                            System.currentTimeMillis()
                    ));
                    NetworkController.getInstance().pushMessage(getActivity(), messageText);
                    textView.setText("");
                }
                return true;
            }
        };
    }

    private void initMessagesList(@NonNull final View view) {
        final ListView messagesListView = (ListView) view.findViewById(R.id.messages_list);

        adapter = new MessagesCursorAdapter(getActivity());
        messagesListView.setAdapter(adapter);
    }

    private void initLoaderManager() {
        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity(), MessagesContract.TABLE_URI, null, null, null, RECEIVED_TIME + " DESC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if (adapter != null && cursor != null) {
                    adapter.changeCursor(cursor);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                if (adapter != null) {
                    adapter.changeCursor(null);
                }
            }
        });
    }
}
