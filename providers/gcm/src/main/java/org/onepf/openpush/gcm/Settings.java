package org.onepf.openpush.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
public class Settings {

    private static final String KEY_REGISTRATION_TOKEN = "registration_token";
    private static final String KEY_APP_VERSION = "app_version";
    private static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_ANDROID_ID = "android_id";

    private static final String PREFS_NAME = "org.onepf.openpush.gcm";

    public static final int NO_SAVED_APP_VERSION = 1;

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

    public int getMessageId() {
        return mPrefs.getInt(KEY_MESSAGE_ID, 1);
    }

    public void saveMessageId(int msgId) {
        mPrefs.edit().putInt(KEY_MESSAGE_ID, msgId).apply();
    }

    public String getAndroidId() {
        return mPrefs.getString(KEY_ANDROID_ID, null);
    }

    public void saveAndroidId(@Nullable String androidId) {
        if (androidId == null) {
            mPrefs.edit().remove(KEY_ANDROID_ID).apply();
        } else {
            mPrefs.edit().putString(KEY_ANDROID_ID, androidId).apply();
        }
    }

    public void reset() {
        mPrefs.edit().clear().apply();
    }
}
