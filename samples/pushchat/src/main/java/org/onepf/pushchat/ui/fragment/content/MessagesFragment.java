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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import org.onepf.pushchat.R;
import org.onepf.pushchat.db.ContentDescriptor.MessagesContract;

import static org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry.*;

/**
 * @author Roman Savin
 * @since 28.04.2015
 */
public class MessagesFragment extends BaseContentFragment {

    private SimpleCursorAdapter adapter;

    @NonNull
    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages, container, false);

        showClearButton();
        initMessagesList(view);
        initLoaderManager();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideClearButton();
    }

    private void initMessagesList(@NonNull final View view) {
        final ListView messagesListView = (ListView) view.findViewById(R.id.messages_list);

        // The desired columns to be bound
        final String[] columns = new String[]{
                SENDER_UUID,
                MESSAGE,
                RECEIVED_TIME
        };

        // the XML defined views which the data will be bound to
        final int[] to = new int[]{
                R.id.sender,
                R.id.message_text,
                R.id.date
        };

        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.item_message,
                null,
                columns,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

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
