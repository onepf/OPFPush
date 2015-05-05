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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import org.onepf.pushchat.R;
import org.onepf.pushchat.ui.adapter.ContactsAdapter;
import org.onepf.pushchat.ui.dialog.AddContactDialogFragment;
import org.onepf.pushchat.utils.ContactsProvider;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class ContactsFragment extends BaseContentFragment {

    private ContactsAdapter adapter;

    @NonNull
    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        adapter = new ContactsAdapter(this, ContactsProvider.getUuids(getActivity()));

        final RecyclerView contactsView = (RecyclerView) view.findViewById(R.id.contacts_view);
        contactsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactsView.setAdapter(adapter);

        final FloatingActionButton addContactFab =
                (FloatingActionButton) view.findViewById(R.id.add_contact_fab);
        addContactFab.attachToRecyclerView(contactsView);

        addContactFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AddContactDialogFragment dialogFragment = AddContactDialogFragment.newInstance();
                dialogFragment.show(getChildFragmentManager(), AddContactDialogFragment.TAG);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
    }

    public void addUuid(@NonNull final String uuid) {
        ContactsProvider.addUuid(getActivity(), uuid);
        if (adapter != null) {
            adapter.addUuid(uuid);
        }
    }
}
