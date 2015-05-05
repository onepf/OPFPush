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

package org.onepf.pushchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author Roman Savin
 * @since 05.05.2015
 */
public final class ContactsProvider {

    private static final String CONTACTS_PREFS_NAME = "org.onepf.pushchat.CONTACTS_PREFS_NAME";

    private static final String UUIDS_KEY = "UUIDS_KEY";

    private ContactsProvider() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static Set<String> getUuids(@NonNull final Context context) {
        //noinspection ConstantConditions
        return context.getSharedPreferences(CONTACTS_PREFS_NAME, MODE_PRIVATE)
                .getStringSet(UUIDS_KEY, new HashSet<String>());
    }

    public static void addUuid(@NonNull final Context context,
                               @NonNull final String uuid) {
        final SharedPreferences contactsPreferences = context
                .getSharedPreferences(CONTACTS_PREFS_NAME, MODE_PRIVATE);
        final Set<String> uuids = contactsPreferences.getStringSet(UUIDS_KEY, new HashSet<String>());
        //noinspection ConstantConditions
        uuids.add(uuid);

        contactsPreferences.edit().putStringSet(UUIDS_KEY, uuids).apply();
    }

    public static void removeUuid(@NonNull final Context context,
                                  @NonNull final String uuid) {
        final SharedPreferences contactsPreferences = context
                .getSharedPreferences(CONTACTS_PREFS_NAME, MODE_PRIVATE);
        final Set<String> uuids = contactsPreferences.getStringSet(UUIDS_KEY, new HashSet<String>());
        //noinspection ConstantConditions
        uuids.remove(uuid);

        contactsPreferences.edit().putStringSet(UUIDS_KEY, uuids).apply();
    }

    public static boolean isUuidAdded(@NonNull final Context context,
                                      @NonNull final String uuid) {
        final Set<String> uuids = context.getSharedPreferences(CONTACTS_PREFS_NAME, MODE_PRIVATE)
                .getStringSet(UUIDS_KEY, new HashSet<String>());
        //noinspection ConstantConditions
        return uuids.contains(uuid);
    }
}
