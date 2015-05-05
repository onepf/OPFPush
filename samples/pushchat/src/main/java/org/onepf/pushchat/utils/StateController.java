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

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.pushchat.model.PushState;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author Roman Savin
 * @since 05.05.2015
 */
public final class StateController {

    private static final String SERVER_STATE_PREFS_NAME = "SERVER_STATE_PREFS_NAME";

    private static final String IS_REGID_SAVED_KEY = "IS_REGID_SAVED_KEY";

    @NonNull
    public static PushState getState(@NonNull final Context context) {
        final OPFPushHelper helper = OPFPush.getHelper();
        if (helper.isRegistered()) {
            if (isRegIdSavedOnServer(context)) {
                //helper is registered and reg id is saved on server;
                return PushState.REGISTERED;
            } else {
                //helper is registered but reg id is no saved on server;
                return PushState.REGISTERING;
            }
        } else if (helper.isRegistering()) {
            return PushState.REGISTERING;
        } else {
            if (isRegIdSavedOnServer(context)) {
                //helper is unregistered but reg id is no removed on server;
                return PushState.UNREGISTERING;
            } else {
                //helper is unregistered and reg id is removed on server;
                return PushState.UNREGISTERED;
            }
        }
    }

    public static void putRegIdSavedOnServerValue(@NonNull final Context context,
                                                  final boolean isSaved) {
        context.getSharedPreferences(SERVER_STATE_PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(IS_REGID_SAVED_KEY, isSaved)
                .apply();
    }

    private static boolean isRegIdSavedOnServer(@NonNull final Context context) {
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(SERVER_STATE_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(IS_REGID_SAVED_KEY, false);
    }
}
