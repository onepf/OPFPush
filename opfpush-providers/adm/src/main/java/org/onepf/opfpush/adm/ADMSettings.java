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

package org.onepf.opfpush.adm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

/**
 * @author Roman Savin
 * @since 27.01.2015
 */
final class ADMSettings {

    public static final int NO_SAVED_APP_VERSION = -1;

    private static final String KEY_REGISTRATION_ID = "registration_id";
    private static final String KEY_APP_VERSION = "app_version";

    private static final String ADM_POSTFIX = "adm";

    private static volatile ADMSettings instance;

    private OPFPreferences preferences;

    private ADMSettings(@NonNull final Context context) {
        preferences = new OPFPreferences(context, ADM_POSTFIX);
    }

    public static ADMSettings getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (ADMSettings.class) {
                if (instance == null) {
                    instance = new ADMSettings(context);
                }
            }
        }

        return instance;
    }

    @Nullable
    public synchronized String getRegistrationId() {
        OPFPushLog.methodD(ADMSettings.class, "getRegistrationId");
        if (getAppVersion() == OPFUtils.getAppVersion(preferences.getContext())) {
            return preferences.getString(KEY_REGISTRATION_ID);
        } else {
            saveRegistrationId(null);
            return null;
        }
    }

    public synchronized void saveRegistrationId(@Nullable final String registrationId) {
        OPFPushLog.methodD(ADMSettings.class, "saveRegistrationId");
        saveAppVersion(OPFUtils.getAppVersion(preferences.getContext()));
        if (registrationId == null) {
            preferences.remove(KEY_REGISTRATION_ID);
        } else {
            preferences.put(KEY_REGISTRATION_ID, registrationId);
        }
    }

    public synchronized void reset() {
        OPFPushLog.methodD(ADMSettings.class, "reset");
        preferences.clear();
    }

    private void saveAppVersion(final int appVersion) {
        OPFPushLog.methodD(ADMSettings.class, "saveAppVersion", appVersion);
        preferences.put(KEY_APP_VERSION, appVersion);
    }

    private int getAppVersion() {
        return preferences.getInt(KEY_APP_VERSION, NO_SAVED_APP_VERSION);
    }
}
