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

package org.onepf.opfpush;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.model.State;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;

import static org.onepf.opfpush.model.State.UNREGISTERED;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 01.10.14.
 */
//TODO: thread safe
final class Settings {

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_STATE = "state";
    private static final String KEY_LAST_ANDROID_ID = "android_id";

    private static volatile Settings instance;

    @NonNull
    private final OPFPreferences preferences;

    private Settings(@NonNull final Context context) {
        preferences = new OPFPreferences(context);
    }

    public static Settings getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    instance = new Settings(context);
                }
            }
        }

        return instance;
    }

    @NonNull
    public State getState() {
        OPFLog.methodD("");

        final int stateValue = preferences.getInt(KEY_STATE, UNREGISTERED.getValue());
        State state = State.fromValue(stateValue);

        OPFLog.d("State : " + state);
        if (state == null) {
            state = UNREGISTERED;
            saveState(state);
        }
        return state;
    }

    public void saveState(@NonNull final State state) {
        OPFLog.methodD(state);
        preferences.put(KEY_STATE, state.getValue());
    }

    public void clear() {
        preferences.clear();
    }

    @Nullable
    public String getLastProviderName() {
        return preferences.getString(KEY_LAST_PROVIDER_NAME);
    }

    public void saveLastProvider(@Nullable final PushProvider provider) {
        OPFLog.methodD(provider);

        if (provider == null) {
            preferences.remove(KEY_LAST_PROVIDER_NAME);
        } else {
            preferences.put(KEY_LAST_PROVIDER_NAME, provider.getName());
        }
    }

    @Nullable
    public String getLastAndroidId() {
        return preferences.getString(KEY_LAST_ANDROID_ID);
    }

    public void saveLastAndroidId(@Nullable final String androidId) {
        OPFLog.methodD(androidId);

        if (androidId == null) {
            preferences.remove(KEY_LAST_ANDROID_ID);
        } else {
            preferences.put(KEY_LAST_ANDROID_ID, androidId);
        }
    }
}
