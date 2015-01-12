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
import org.onepf.opfutils.OPFPreferences;

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
class Settings {

    private static final long STATE_INFINITY_TIMESTAMP = -1L;

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_STATE = "state";
    private static final String KEY_LAST_ANDROID_ID = "android_id";
    private static final String KEY_STATE_TIMESTAMP = "state_timestamp";

    @NonNull
    private final OPFPreferences preferences;

    public Settings(@NonNull final Context context) {
        preferences = new OPFPreferences(context);
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

        preferences.put(KEY_STATE_TIMESTAMP, stateTimestamp);
    }

    public void clear() {
        preferences.clear();
    }

    @Nullable
    public String getLastProviderName() {
        return preferences.getString(KEY_LAST_PROVIDER_NAME);
    }

    public void saveLastProvider(@Nullable final PushProvider provider) {
        OPFPushLog.methodD(Settings.class, "saveLastProvider", provider);

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
        OPFPushLog.methodD(Settings.class, "saveLastAndroidId", androidId);

        if (androidId == null) {
            preferences.remove(KEY_LAST_ANDROID_ID);
        } else {
            preferences.put(KEY_LAST_ANDROID_ID, androidId);
        }
    }

    private boolean isInfinityState(@NonNull final State state) {
        return state == REGISTERED || state == UNREGISTERED;
    }
}
