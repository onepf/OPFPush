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

import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;
import org.onepf.opfutils.OPFUtils;

/**
 * @author Roman Savin
 * @since 27.01.2015
 */
final class PreferencesProvider {

    public static final int NO_SAVED_APP_VERSION = -1;

    private static final String KEY_REGISTRATION_ID = "registration_id";
    private static final String KEY_APP_VERSION = "app_version";
    private static final String KEY_AUTHENTICATION_FAILED_FLAG = "authentication_failed_flag";

    private static final String ADM_POSTFIX = "adm";

    private static volatile PreferencesProvider instance;

    private OPFPreferences preferences;

    private PreferencesProvider(@NonNull final Context context) {
        preferences = new OPFPreferences(context, ADM_POSTFIX);
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
        OPFLog.methodD();
        updateAppVersion();
        return preferences.getString(KEY_REGISTRATION_ID);
    }

    public synchronized void saveRegistrationId(@Nullable final String registrationId) {
        OPFLog.methodD();
        updateAppVersion();
        if (registrationId == null) {
            preferences.remove(KEY_REGISTRATION_ID);
        } else {
            preferences.put(KEY_REGISTRATION_ID, registrationId);
        }
    }

    public synchronized boolean isAuthenticationFailed() {
        updateAppVersion();
        return preferences.getBoolean(KEY_AUTHENTICATION_FAILED_FLAG, false);
    }

    public synchronized void saveAuthenticationFailedFlag() {
        OPFLog.methodD();

        updateAppVersion();
        preferences.put(KEY_AUTHENTICATION_FAILED_FLAG, true);
    }

    public synchronized void removeAuthenticationFailedFlag() {
        OPFLog.methodD();

        updateAppVersion();
        preferences.remove(KEY_AUTHENTICATION_FAILED_FLAG);
    }

    public synchronized void reset() {
        OPFLog.methodD();
        preferences.clear();
    }

    private void updateAppVersion() {
        if (getAppVersion() != OPFUtils.getAppVersion(preferences.getContext())) {
            reset();
            saveAppVersion(OPFUtils.getAppVersion(preferences.getContext()));
        }
    }

    private void saveAppVersion(final int appVersion) {
        OPFLog.methodD(appVersion);
        preferences.put(KEY_APP_VERSION, appVersion);
    }

    private int getAppVersion() {
        return preferences.getInt(KEY_APP_VERSION, NO_SAVED_APP_VERSION);
    }
}
