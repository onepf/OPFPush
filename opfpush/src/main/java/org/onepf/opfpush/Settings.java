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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.model.State;

import java.util.concurrent.TimeUnit;

import static org.onepf.opfpush.model.State.REGISTERED;
import static org.onepf.opfpush.model.State.REGISTERING;
import static org.onepf.opfpush.model.State.UNREGISTERED;
import static org.onepf.opfpush.model.State.UNREGISTERING;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 01.10.14.
 */
//TODO: Config timeout via options.
class Settings {

    private static final long STATE_INFINITY_TIMESTAMP = -1L;

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_STATE = "state";
    private static final String KEY_LAST_ANDROID_ID = "android_id";
    private static final String KEY_STATE_TIMESTAMP = "state_timestamp";

    private static final String PREF_NAME = "org.onepf.openpush";

    @NonNull
    private final SharedPreferences preferences;

    public Settings(@NonNull final Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public State getState() {
        OPFPushLog.methodD(Settings.class, "getState");

        final int stateValue = preferences.getInt(KEY_STATE, UNREGISTERED.getValue());
        State state = State.fromValue(stateValue);

        OPFPushLog.d("State : " + state);
        if (state == null) {
            state = UNREGISTERED;
            saveState(state);
        }

        final long stateTimestamp = preferences.getLong(KEY_STATE_TIMESTAMP, STATE_INFINITY_TIMESTAMP);
        final long currentTime = System.currentTimeMillis();

        OPFPushLog.d("State timestamp : " + stateTimestamp);
        if (stateTimestamp != STATE_INFINITY_TIMESTAMP
                && currentTime - stateTimestamp > TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
            OPFPushLog.d("state timestamp ");

            if (state == REGISTERING) {
                state = UNREGISTERED;
                saveState(state);
            } else if (state == UNREGISTERING) {
                state = REGISTERED;
                saveState(state);
            }
        }

        return state;
    }

    public void saveState(@NonNull final State state) {
        OPFPushLog.methodD(Settings.class, "saveState", state);

        final long stateTimestamp = isInfinityState(state) ? STATE_INFINITY_TIMESTAMP
                : System.currentTimeMillis();

        preferences.edit()
                .putInt(KEY_STATE, state.getValue())
                .putLong(KEY_STATE_TIMESTAMP, stateTimestamp)
                .apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    @Nullable
    public String getLastProviderName() {
        return preferences.getString(KEY_LAST_PROVIDER_NAME, null);
    }

    public void saveLastProvider(@Nullable final PushProvider provider) {
        OPFPushLog.methodD(Settings.class, "saveLastProvider", provider);

        final SharedPreferences.Editor editor = preferences.edit();
        if (provider == null) {
            editor.remove(KEY_LAST_PROVIDER_NAME);
        } else {
            editor.putString(KEY_LAST_PROVIDER_NAME, provider.getName());
        }
        editor.apply();
    }

    @Nullable
    public String getLastAndroidId() {
        return preferences.getString(KEY_LAST_ANDROID_ID, null);
    }

    public void saveLastAndroidId(@Nullable final String androidId) {
        OPFPushLog.methodD(Settings.class, "saveLastAndroidId", androidId);

        final SharedPreferences.Editor editor = preferences.edit();
        if (androidId == null) {
            editor.remove(KEY_LAST_ANDROID_ID);
        } else {
            editor.putString(KEY_LAST_ANDROID_ID, androidId);
        }
        editor.apply();
    }

    private boolean isInfinityState(@NonNull final State state) {
        return state == REGISTERED || state == UNREGISTERED;
    }
}
