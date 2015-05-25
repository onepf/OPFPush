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

package org.onepf.pushchat.db;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
public final class ContentDescriptor {

    private static final String AUTHORITY = "org.onepf.pushchat.db.databaseContentProvider";
    private static final Uri CONTENT_BASE_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .build();

    private ContentDescriptor() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("PMD.NonStaticInitializer")
    public static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH) {
        {
            addURI(AUTHORITY, MessagesContract.TABLE_NAME, MessagesContract.ALL_URI_CODE);
            addURI(AUTHORITY, MessagesContract.TABLE_NAME + "/#/", MessagesContract.URI_CODE);

            addURI(AUTHORITY, ContactsContract.TABLE_NAME, ContactsContract.ALL_URI_CODE);
            addURI(AUTHORITY, ContactsContract.TABLE_NAME + "/#/", ContactsContract.URI_CODE);
        }

        @Override
        public int match(@NonNull final Uri uri) {
            final int result = super.match(uri);
            if (result < 0) {
                throw new IllegalArgumentException("URI " + uri.toString() + " could not be matched.");
            } else {
                return result;
            }
        }

    };

    public static class MessagesContract {
        public static final String TABLE_NAME = "messages";

        public static final Uri TABLE_URI = CONTENT_BASE_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static final int ALL_URI_CODE = 0;
        public static final int URI_CODE = 1;

        public static class MessageEntry {
            public static final String ID = BaseColumns._ID;
            public static final String SENDER_UUID = "sender_uuid";
            public static final String SENDER_NAME = "sender_name";
            public static final String MESSAGE = "message";
            public static final String RECEIVED_TIME = "received_time";
        }
    }

    public static class ContactsContract {
        public static final String TABLE_NAME = "contacts";

        public static final Uri TABLE_URI = CONTENT_BASE_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static final int ALL_URI_CODE = 2;
        public static final int URI_CODE = 3;

        public static class ContactEntry {
            public static final String ID = BaseColumns._ID;
            public static final String UUID = "uuid";
            public static final String NAME = "name";
        }
    }

    public static String getTableName(int uriCode) {
        switch (uriCode) {
            case MessagesContract.ALL_URI_CODE:
            case MessagesContract.URI_CODE:
                return MessagesContract.TABLE_NAME;
            case ContactsContract.ALL_URI_CODE:
            case ContactsContract.URI_CODE:
                return ContactsContract.TABLE_NAME;
        }
        throw new IllegalArgumentException("uriCode " + uriCode);
    }
}
