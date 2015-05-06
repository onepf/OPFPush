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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import org.onepf.pushchat.db.ContentDescriptor.MessagesContract;
import org.onepf.pushchat.model.Message;

import static org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry.*;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "org.onepf.pushchat.db";
    private static final int DATABASE_VERSION = 1;

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + MessagesContract.TABLE_NAME + " ( " +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + COMMA_SEP +
                    SENDER_UUID + " TEXT NOT NULL" + COMMA_SEP +
                    MESSAGE + " TEXT NOT NULL" + COMMA_SEP +
                    RECEIVED_TIME + " LONG" +
                    " )";

    private static final String SQL_DELETE_MESSAGES_TABLE = "DROP TABLE IF EXISTS " +
            MessagesContract.TABLE_NAME;


    @NonNull
    private final AsyncQueryHandler asyncQueryHandler;

    private volatile static DatabaseHelper instance = null;

    private DatabaseHelper(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.asyncQueryHandler = new MyAsyncQueryHandler(context.getContentResolver());
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
        sqLiteDatabase.execSQL(SQL_CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase sqLiteDatabase,
                          final int oldVersion,
                          final int newVersion) {
        if (oldVersion < newVersion) {
            sqLiteDatabase.execSQL(SQL_DELETE_MESSAGES_TABLE);
            onCreate(sqLiteDatabase);
        }
    }

    public void addMessage(@NonNull final Message message) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(SENDER_UUID, message.getSenderUuid());
        contentValues.put(MESSAGE, message.getMessage());
        contentValues.put(RECEIVED_TIME, message.getReceivedTime());

        asyncQueryHandler.startInsert(1, null, ContentDescriptor.MessagesContract.TABLE_URI, contentValues);
    }

    private static class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(@NonNull final ContentResolver contentResolver) {
            super(contentResolver);
        }
    }
}
