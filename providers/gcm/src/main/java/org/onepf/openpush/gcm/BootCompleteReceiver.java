package org.onepf.openpush.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import org.jetbrains.annotations.NotNull;
import org.onepf.openpush.OpenPushHelper;

/**
 * Created by krozov on 09.09.14.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NotNull Context context, Intent intent) {
        if (!Settings.Secure.ANDROID_ID.equals(getLastAndroidId(context))) {
            saveAndroidId(context);
            OpenPushHelper.getInstance(context).onProviderNeedUpdate(GCMProvider.NAME);
        }
    }

    private static String getLastAndroidId(@NotNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(GCMProvider.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(GCMProvider.PREF_ANDROID_ID, null);
    }

    private static void saveAndroidId(@NotNull Context context) {
        SharedPreferences.Editor editor = context.getApplicationContext()
                .getSharedPreferences(GCMProvider.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(GCMProvider.PREF_ANDROID_ID, Settings.Secure.ANDROID_ID)
                .apply();
    }
}
