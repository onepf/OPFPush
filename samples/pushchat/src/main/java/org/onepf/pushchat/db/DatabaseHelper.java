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

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import org.onepf.pushchat.db.ContentDescriptor.ContactsContract;
import org.onepf.pushchat.db.ContentDescriptor.ContactsContract.ContactEntry;
import org.onepf.pushchat.db.ContentDescriptor.MessagesContract;
import org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry;
import org.onepf.pushchat.db.DatabaseHelper.ContactsUuidsAsyncQueryHandler.QueryContactsUuidsCallback;
import org.onepf.pushchat.model.db.Contact;
import org.onepf.pushchat.model.db.Message;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "org.onepf.pushchat.db";
    private static final int DATABASE_VERSION = 1;

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + ContactsContract.TABLE_NAME + " ( " +
                    ContactEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + COMMA_SEP +
                    ContactEntry.NAME + " TEXT" + COMMA_SEP +
                    ContactEntry.UUID + " TEXT UNIQUE NOT NULL" +
                    " )";

    private static final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + MessagesContract.TABLE_NAME + " ( " +
                    MessageEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + COMMA_SEP +
                    MessageEntry.SENDER_UUID + " TEXT NOT NULL" + COMMA_SEP +
                    MessageEntry.SENDER_NAME + " TEXT" + COMMA_SEP +
                    MessageEntry.MESSAGE + " TEXT NOT NULL" + COMMA_SEP +
                    MessageEntry.RECEIVED_TIME + " LONG" +
                    " )";

    private static final String SQL_DELETE_CONTACTS_TABLE = "DROP TABLE IF EXISTS " +
            ContactsContract.TABLE_NAME;

    private static final String SQL_DELETE_MESSAGES_TABLE = "DROP TABLE IF EXISTS " +
            MessagesContract.TABLE_NAME;

    private static final int MESSAGE_OPERATION_TOKEN = 1;
    private static final int CONTACT_OPERATION_TOKEN = 2;
    private static final int QUERY_UUIDS_TOKEN = 3;
    private static final int QUERY_CONTACT_NAME_TOKEN = 4;

    @NonNull
    private final Context appContext;

    @NonNull
    private final AsyncQueryHandler asyncQueryHandler;

    private volatile static DatabaseHelper instance;

    private DatabaseHelper(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
        this.asyncQueryHandler = new CommonAsyncQueryHandler(context.getContentResolver());
    }

    public synchronized static DatabaseHelper getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context);
                }
            }
        }

        return instance;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_CONTACTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase sqLiteDatabase,
                          final int oldVersion,
                          final int newVersion) {
        if (oldVersion < newVersion) {
            sqLiteDatabase.execSQL(SQL_DELETE_MESSAGES_TABLE);
            sqLiteDatabase.execSQL(SQL_DELETE_CONTACTS_TABLE);
            onCreate(sqLiteDatabase);
        }
    }

    public void addMessage(@NonNull final Message message) {
        new QueryContactNameQueryHandler(appContext.getContentResolver(), queryContactNameCallback(message))
                .startQuery(
                        QUERY_CONTACT_NAME_TOKEN,
                        null,
                        ContactsContract.TABLE_URI,
                        new String[]{ContactEntry.NAME},
                        ContactEntry.UUID + "=?",
                        new String[]{message.getSenderUuid()},
                        null
                );
    }

    private QueryContactNameQueryHandler.QueryContactNameCallback queryContactNameCallback(
            @NonNull final Message message
    ) {
        return new QueryContactNameQueryHandler.QueryContactNameCallback() {
            @Override
            public void onComplete(@NonNull final String name) {
                final ContentValues contentValues = new ContentValues();
                contentValues.put(MessageEntry.SENDER_UUID, message.getSenderUuid());
                contentValues.put(MessageEntry.SENDER_NAME, name);
                contentValues.put(MessageEntry.MESSAGE, message.getMessage());
                contentValues.put(MessageEntry.RECEIVED_TIME, message.getReceivedTime());

                asyncQueryHandler.startInsert(MESSAGE_OPERATION_TOKEN, null, MessagesContract.TABLE_URI, contentValues);
            }
        };
    }

    public void deleteMessages() {
        asyncQueryHandler.startDelete(MESSAGE_OPERATION_TOKEN, null, MessagesContract.TABLE_URI, null, null);
    }

    public void addContact(@NonNull final Contact contact) {
        final String contactName = contact.getName();
        final String contactUuid = contact.getUuid();

        final ContentValues contactContentValues = new ContentValues();
        contactContentValues.put(ContactEntry.NAME, contactName);
        contactContentValues.put(ContactEntry.UUID, contactUuid);

        asyncQueryHandler.startInsert(CONTACT_OPERATION_TOKEN, null, ContactsContract.TABLE_URI, contactContentValues);

        final ContentValues messageContentValues = new ContentValues();
        messageContentValues.put(MessageEntry.SENDER_NAME, contactName);
        asyncQueryHandler.startUpdate(
                MESSAGE_OPERATION_TOKEN,
                null,
                MessagesContract.TABLE_URI,
                messageContentValues,
                MessageEntry.SENDER_UUID + "=?",
                new String[]{contactUuid}
        );
    }

    public void deleteContact(final long id) {
        asyncQueryHandler.startDelete(
                CONTACT_OPERATION_TOKEN,
                null,
                ContactsContract.TABLE_URI,
                ContactEntry.ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public void queryAllContactsUuids(@NonNull final QueryContactsUuidsCallback callback) {
        new ContactsUuidsAsyncQueryHandler(appContext.getContentResolver(), callback).startQuery(
                QUERY_UUIDS_TOKEN,
                null,
                ContactsContract.TABLE_URI,
                new String[]{ContactEntry.UUID},
                null,
                null,
                null
        );
    }

    public boolean isUuidAdded(@NonNull final String uuid) {
        final Cursor cursor = getReadableDatabase().query(
                ContactsContract.TABLE_NAME,
                new String[]{ContactEntry.UUID},
                ContactEntry.UUID + "=?",
                new String[]{uuid},
                null,
                null,
                null
        );

        final boolean isUuidAdded = cursor.getCount() > 0;
        cursor.close();
        return isUuidAdded;
    }

    public boolean isContactsTableEmpty() {
        final Cursor cursor = getReadableDatabase().query(
                ContactsContract.TABLE_NAME,
                new String[]{ContactEntry.ID},
                null,
                null,
                null,
                null,
                null
        );

        final boolean isEmpty = cursor.getCount() == 0;
        cursor.close();
        return isEmpty;
    }

    @NonNull
    public String querySenderUuidById(final long id) {
        final Cursor cursor = getReadableDatabase().query(
                MessagesContract.TABLE_NAME,
                new String[]{MessageEntry.SENDER_UUID},
                MessageEntry.ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        String senderUuid = "";
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            senderUuid = cursor.getString(cursor.getColumnIndexOrThrow(MessageEntry.SENDER_UUID));
        }
        cursor.close();
        return senderUuid;
    }

    public static class ContactsUuidsAsyncQueryHandler extends AsyncQueryHandler {

        @NonNull
        private final QueryContactsUuidsCallback callback;

        public ContactsUuidsAsyncQueryHandler(@NonNull final ContentResolver contentResolver,
                                              @NonNull final QueryContactsUuidsCallback callback) {
            super(contentResolver);
            this.callback = callback;
        }

        @Override
        protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor) {
            final Set<String> uuids = new HashSet<>();
            if (token == QUERY_UUIDS_TOKEN) {
                cursor.moveToFirst();
                while (true) {
                    uuids.add(cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.UUID)));
                    if (cursor.isLast()) {
                        break;
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            callback.onComplete(uuids);
        }

        public interface QueryContactsUuidsCallback {

            void onComplete(@NonNull final Set<String> uuids);
        }
    }

    private static class CommonAsyncQueryHandler extends AsyncQueryHandler {

        public CommonAsyncQueryHandler(@NonNull final ContentResolver contentResolver) {
            super(contentResolver);
        }
    }

    private static class QueryContactNameQueryHandler extends AsyncQueryHandler {

        @NonNull
        private final QueryContactNameCallback callback;

        public QueryContactNameQueryHandler(@NonNull final ContentResolver contentResolver,
                                            @NonNull final QueryContactNameCallback callback) {
            super(contentResolver);
            this.callback = callback;
        }

        @Override
        protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor) {
            if (token == QUERY_CONTACT_NAME_TOKEN) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    callback.onComplete(cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.NAME)));
                } else {
                    callback.onComplete("");
                }
            }
            cursor.close();
        }

        private interface QueryContactNameCallback {

            void onComplete(@NonNull final String name);
        }
    }
}
