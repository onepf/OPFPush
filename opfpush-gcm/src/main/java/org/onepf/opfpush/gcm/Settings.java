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

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
class Settings {

    private static final String KEY_REGISTRATION_TOKEN = "registration_token";
    private static final String KEY_APP_VERSION = "app_version";

    private static final String PREFS_NAME = "org.onepf.openpush.gcm";

    public static final int NO_SAVED_APP_VERSION = -1;

    private SharedPreferences mPrefs;

    public Settings(@NonNull Context context) {
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getRegistrationToken() {
        return mPrefs.getString(KEY_REGISTRATION_TOKEN, null);
    }

    public void saveRegistrationToken(@Nullable String token) {
        if (token == null) {
            mPrefs.edit().remove(KEY_REGISTRATION_TOKEN).apply();
        } else {
            mPrefs.edit().putString(KEY_REGISTRATION_TOKEN, token).apply();
        }
    }

    public int getAppVersion() {
        return mPrefs.getInt(KEY_APP_VERSION, NO_SAVED_APP_VERSION);
    }

    public void saveAppVersion(int appVersion) {
        mPrefs.edit().putInt(KEY_APP_VERSION, appVersion).apply();
    }

    public void removeAppVersion() {
        mPrefs.edit().remove(KEY_APP_VERSION).apply();
    }

    public void reset() {
        mPrefs.edit().clear().apply();
    }
}
