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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 01.10.14.
 */
final class PreferencesProvider {

    private static final String KEY_REGISTRATION_ID = "registration_id";
    private static final String KEY_APP_VERSION = "app_version";

    private static final String GCM_POSTFIX = "gcm";

    public static final int NO_SAVED_APP_VERSION = -1;

    private static volatile PreferencesProvider instance;

    private OPFPreferences preferences;

    private PreferencesProvider(@NonNull final Context context) {
        preferences = new OPFPreferences(context, GCM_POSTFIX);
    }

    public static PreferencesProvider getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (PreferencesProvider.class) {
                if (instance == null) {
                    instance = new PreferencesProvider(context);
                }
            }
        }

        return instance;
    }

    @Nullable
    public synchronized String getRegistrationId() {
        OPFLog.methodD(PreferencesProvider.class, "getRegistrationId");
        if (getAppVersion() == OPFUtils.getAppVersion(preferences.getContext())) {
            return preferences.getString(KEY_REGISTRATION_ID);
        } else {
            saveRegistrationId(null);
            return null;
        }
    }

    public synchronized void saveRegistrationId(@Nullable final String registrationId) {
        OPFLog.methodD(PreferencesProvider.class, "saveRegistrationId", "registrationId");
        saveAppVersion(OPFUtils.getAppVersion(preferences.getContext()));
        if (registrationId == null) {
            preferences.remove(KEY_REGISTRATION_ID);
        } else {
            preferences.put(KEY_REGISTRATION_ID, registrationId);
        }
    }

    public synchronized void reset() {
        OPFLog.methodD(PreferencesProvider.class, "reset");
        preferences.clear();
    }

    private int getAppVersion() {
        return preferences.getInt(KEY_APP_VERSION, NO_SAVED_APP_VERSION);
    }

    private void saveAppVersion(final int appVersion) {
        OPFLog.methodD(PreferencesProvider.class, "saveAppVersion", appVersion);
        preferences.put(KEY_APP_VERSION, appVersion);
    }
}
