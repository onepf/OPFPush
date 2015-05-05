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

package org.onepf.pushchat.ui.adapter;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.onepf.pushchat.R;
import org.onepf.pushchat.ui.dialog.AlertDialogFragment;
import org.onepf.pushchat.ui.fragment.content.ContactsFragment;
import org.onepf.pushchat.utils.ContactsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Savin
 * @since 05.05.2015
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    @NonNull
    private final ContactsFragment contactsFragment;

    @NonNull
    private List<String> uuids;

    public ContactsAdapter(@NonNull final ContactsFragment contactsFragment,
                           @NonNull final Set<String> uuids) {
        this.contactsFragment = contactsFragment;
        this.uuids = new ArrayList<>(uuids);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.uuidTextView.setText(uuids.get(position));
    }

    @Override
    public int getItemCount() {
        return uuids.size();
    }

    public void addUuid(@NonNull final String uuid) {
        if (!uuids.contains(uuid)) {
            uuids.add(uuid);
            notifyDataSetChanged();
        }
    }

    public void removeUuid(@NonNull final String uuid) {
        uuids.remove(uuid);
        notifyDataSetChanged();
    }


    private View.OnLongClickListener onLongClickListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final String removingUuid = ((TextView) view).getText().toString();

                final AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(
                        contactsFragment.getActivity().getString(R.string.remove_contact_question)
                );
                dialogFragment.setOnClickListener(onDialogClickListener(removingUuid));
                dialogFragment.show(contactsFragment.getChildFragmentManager(), AlertDialogFragment.TAG);
                return true;
            }
        };
    }

    private DialogInterface.OnClickListener onDialogClickListener(@NonNull final String removingUuid) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContactsProvider.removeUuid(contactsFragment.getActivity(), removingUuid);
                removeUuid(removingUuid);
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView uuidTextView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.uuidTextView = (TextView) itemView;
            this.uuidTextView.setOnLongClickListener(onLongClickListener());
        }
    }
}
