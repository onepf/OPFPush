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

package org.onepf.openpush.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.NonNull;

import org.onepf.openpush.OpenPushHelper;

/**
 * @author Kirill Rozov
 * @since 09.09.14.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        if (!Settings.Secure.ANDROID_ID.equals(getLastAndroidId(context))) {
            saveAndroidId(context);
            OpenPushHelper.getInstance(context).getProviderCallback().onNeedRetryRegister(GCMProvider.NAME);
        }
    }

    private static String getLastAndroidId(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(GCMProvider.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(GCMProvider.PREF_ANDROID_ID, null);
    }

    private static void saveAndroidId(@NonNull Context context) {
        SharedPreferences.Editor editor = context.getApplicationContext()
                .getSharedPreferences(GCMProvider.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(GCMProvider.PREF_ANDROID_ID, Settings.Secure.ANDROID_ID)
                .apply();
    }
}
