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

import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ListView;
import com.melnykov.fab.FloatingActionButton;
import org.onepf.pushchat.R;
import org.onepf.pushchat.db.ContentDescriptor;
import org.onepf.pushchat.db.DatabaseHelper;
import org.onepf.pushchat.ui.adapter.ContactsCursorAdapter;
import org.onepf.pushchat.ui.dialog.AddContactDialogFragment;
import org.onepf.pushchat.ui.dialog.AlertDialogFragment;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class ContactsFragment extends BaseContentFragment {

    private ContactsCursorAdapter adapter;
    private ListView contactsListView;
    private LoaderManager.LoaderCallbacks loaderCallbacks;

    @NonNull
    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        initContactsList(view);
        initFAB(view);
        initLoaderManager();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loaderCallbacks = null;
        adapter = null;
        contactsListView = null;
    }

    @Override
    public int getTitleResId() {
        return R.string.title_contacts_fragment;
    }

    private void initContactsList(@NonNull final View view) {
        adapter = new ContactsCursorAdapter(getActivity());

        contactsListView = (ListView) view.findViewById(R.id.contacts_list);
        contactsListView.setAdapter(adapter);

        contactsListView.setOnItemLongClickListener(onItemLongClick());
    }

    private void initFAB(@NonNull final View view) {
        final FloatingActionButton addContactFab =
                (FloatingActionButton) view.findViewById(R.id.add_contact_fab);
        addContactFab.attachToListView(contactsListView);
        addContactFab.setOnClickListener(onFabClickListener());
    }

    private void initLoaderManager() {
        final LoaderManager loaderManager = getLoaderManager();
        loaderCallbacks = new ContactsLoaderCallbacks();
        loaderManager.initLoader(0, null, loaderCallbacks);
    }

    private AdapterView.OnItemLongClickListener onItemLongClick() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                //Show dialog which suggests to remove contact
                final AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(
                        getActivity().getString(R.string.remove_contact_question)
                );
                dialogFragment.setOnClickListener(onDialogClickListener(id));
                dialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
                return true;
            }
        };
    }

    private DialogInterface.OnClickListener onDialogClickListener(final long removingContactId) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                //delete contact from DB.
                DatabaseHelper.getInstance(getActivity()).deleteContact(removingContactId);
            }
        };
    }

    private View.OnClickListener onFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //Show dialog which suggests to add new contact.
                final AddContactDialogFragment dialogFragment = AddContactDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), AddContactDialogFragment.TAG);
            }
        };
    }

    private class ContactsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), ContentDescriptor.ContactsContract.TABLE_URI, null, null, null, null);
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
    }
}
