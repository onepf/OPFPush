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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public class DatabaseContentProvider extends ContentProvider {

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(final Uri uri,
                        final String[] projection,
                        final String selection,
                        final String[] selectionArgs,
                        final String orderBy) {
        final int uriType = ContentDescriptor.URI_MATCHER.match(uri);
        final String tableName = ContentDescriptor.getTableName(uriType);

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final Cursor cursor = db.query(tableName, projection, selection, selectionArgs, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(final Uri uri) {
        return null;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues contentValues) {
        final int uriType = ContentDescriptor.URI_MATCHER.match(uri);
        final String tableName = ContentDescriptor.getTableName(uriType);

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final long id = db.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);

        getContext().getContentResolver().notifyChange(uri, null);

        if (TextUtils.equals(tableName, ContentDescriptor.MessagesContract.TABLE_NAME)) {
            return Uri.parse(ContentDescriptor.MessagesContract.TABLE_URI + "/" + id);
        } else {
            return null;
        }
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final int uriType = ContentDescriptor.URI_MATCHER.match(uri);
        final String tableName = ContentDescriptor.getTableName(uriType);

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int result = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int update(final Uri uri,
                      final ContentValues contentValues,
                      final String selection,
                      final String[] selectionArgs) {
        final int uriType = ContentDescriptor.URI_MATCHER.match(uri);
        final String tableName = ContentDescriptor.getTableName(uriType);

        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.update(tableName, contentValues, selection, selectionArgs);
    }
}
