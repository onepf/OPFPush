/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.opfpush.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.OPFPushLog;

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
class GCMSettings {

    private static final String KEY_REGISTRATION_TOKEN = "registration_token";
    private static final String KEY_APP_VERSION = "app_version";

    private static final String PREFS_NAME = "org.onepf.openpush.gcm";

    public static final int NO_SAVED_APP_VERSION = -1;

    private SharedPreferences preferences;

    public GCMSettings(@NonNull final Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getRegistrationId() {
        return preferences.getString(KEY_REGISTRATION_TOKEN, null);
    }

    public void saveRegistrationId(@Nullable final String registrationId) {
        OPFPushLog.methodD(GCMSettings.class, "saveRegistrationId", "registrationId");

        if (registrationId == null) {
            preferences.edit().remove(KEY_REGISTRATION_TOKEN).apply();
        } else {
            preferences.edit().putString(KEY_REGISTRATION_TOKEN, registrationId).apply();
        }
    }

    public int getAppVersion() {
        return preferences.getInt(KEY_APP_VERSION, NO_SAVED_APP_VERSION);
    }

    public void saveAppVersion(final int appVersion) {
        OPFPushLog.methodD(GCMSettings.class, "saveAppVersion", appVersion);
        preferences.edit().putInt(KEY_APP_VERSION, appVersion).apply();
    }

    public void removeAppVersion() {
        OPFPushLog.methodD(GCMSettings.class, "removeAppVersion");
        preferences.edit().remove(KEY_APP_VERSION).apply();
    }

    public void reset() {
        OPFPushLog.methodD(GCMSettings.class, "reset");
        preferences.edit().clear().apply();
    }
}
