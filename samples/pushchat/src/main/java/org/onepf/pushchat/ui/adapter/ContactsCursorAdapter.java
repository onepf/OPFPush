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

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import org.onepf.pushchat.R;

import static org.onepf.pushchat.db.ContentDescriptor.ContactsContract.ContactEntry.NAME;
import static org.onepf.pushchat.db.ContentDescriptor.ContactsContract.ContactEntry.UUID;

/**
 * @author Roman Savin
 * @since 07.05.2015
 */
public class ContactsCursorAdapter extends CursorAdapter {

    public ContactsCursorAdapter(@NonNull final Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
        final String uuid = cursor.getString(cursor.getColumnIndexOrThrow(UUID));

        final TextView nameTextView = (TextView) view.findViewById(R.id.name_text);
        final TextView uuidTextView = (TextView) view.findViewById(R.id.uuid_text);

        nameTextView.setText(context.getString(R.string.contact_name_fmt, name));
        uuidTextView.setText(context.getString(R.string.contact_uuid_fmt, uuid));
    }
}
