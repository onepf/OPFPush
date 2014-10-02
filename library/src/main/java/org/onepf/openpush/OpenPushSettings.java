package org.onepf.openpush;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
public class OpenPushSettings {

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_STATE = "state";
    private static final String KEY_LAST_ANDROID_ID = "android_id";

    private static final String PREF_NAME = "org.onepf.openpush";

    @NonNull
    private final SharedPreferences mPreferences;

    public OpenPushSettings(@NonNull Context context) {
        mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @OpenPushHelper.State
    public int getState() {
        @OpenPushHelper.State int state = mPreferences.getInt(KEY_STATE, OpenPushHelper.STATE_UNREGISTERED);
        return state;
    }

    public void saveState(@OpenPushHelper.State int state) {
        mPreferences.edit().putInt(KEY_STATE, state).apply();
    }

    public void clear(){
        mPreferences.edit().clear().apply();
    }

    @Nullable
    public String getLastProviderName() {
        return mPreferences.getString(KEY_LAST_PROVIDER_NAME, null);
    }

    public void saveLastProvider(@Nullable PushProvider provider) {
        if (provider == null) {
            mPreferences.edit().remove(KEY_LAST_PROVIDER_NAME).apply();
        } else {
            mPreferences.edit().putString(KEY_LAST_PROVIDER_NAME, provider.getName()).apply();
        }
    }

    @Nullable
    public String getLastAndroidId() {
        return mPreferences.getString(KEY_LAST_ANDROID_ID, null);
    }

    public void saveLastAndroidId(@Nullable String androidId) {
        if (androidId == null) {
            mPreferences.edit().remove(KEY_LAST_ANDROID_ID).apply();
        } else {
            mPreferences.edit().putString(KEY_LAST_ANDROID_ID, androidId).apply();
        }
    }
}
