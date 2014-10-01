package org.onepf.openpush;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
public class Settings {

    static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    static final String KEY_STATE = "state";
    static final String PREF_NAME = "org.onepf.openpush";

    @NonNull
    private final SharedPreferences mPreferences;

    public Settings(@NonNull Context context) {
        mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @OpenPushHelper.State
    public int getState() {
        return mPreferences.getInt(KEY_STATE, OpenPushHelper.STATE_NONE);
    }

    public void saveState(@OpenPushHelper.State int state) {
        mPreferences.edit().putInt(KEY_STATE, state).apply();
    }

    @Nullable
    public String getLastProviderName() {
        return mPreferences.getString(KEY_LAST_PROVIDER_NAME, null);
    }

    public void clear() {
        mPreferences.edit().clear().apply();
    }

    public void saveLastProvider(@Nullable PushProvider provider) {
        if (provider == null) {
            mPreferences.edit().remove(KEY_LAST_PROVIDER_NAME).apply();
        } else {
            mPreferences.edit().putString(KEY_LAST_PROVIDER_NAME, provider.getName()).apply();
        }
    }
}
