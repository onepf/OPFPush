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

import static org.onepf.opfpush.model.State.UNREGISTERED;

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
//TODO Add timeout for storing state
class Settings {

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_STATE = "state";
    private static final String KEY_LAST_ANDROID_ID = "android_id";

    private static final String PREF_NAME = "org.onepf.openpush";

    @NonNull
    private final SharedPreferences mPreferences;

    public Settings(@NonNull Context context) {
        mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public State getState() {
        final int stateValue = mPreferences.getInt(KEY_STATE, UNREGISTERED.getValue());
        final State state = State.fromValue(stateValue);
        return state == null ? UNREGISTERED : state;
    }

    public void saveState(@NonNull final State state) {
        mPreferences.edit().putInt(KEY_STATE, state.getValue()).apply();
    }

    public void clear() {
        mPreferences.edit().clear().apply();
    }

    @Nullable
    public String getLastProviderName() {
        return mPreferences.getString(KEY_LAST_PROVIDER_NAME, null);
    }

    public void saveLastProvider(@Nullable PushProvider provider) {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (provider == null) {
            editor.remove(KEY_LAST_PROVIDER_NAME);
        } else {
            editor.putString(KEY_LAST_PROVIDER_NAME, provider.getName());
        }
        editor.apply();
    }

    @Nullable
    public String getLastAndroidId() {
        return mPreferences.getString(KEY_LAST_ANDROID_ID, null);
    }

    public void saveLastAndroidId(@Nullable String androidId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        if (androidId == null) {
            editor.remove(KEY_LAST_ANDROID_ID);
        } else {
            editor.putString(KEY_LAST_ANDROID_ID, androidId);
        }
        editor.apply();
    }
}
