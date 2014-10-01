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
import android.support.annotation.NonNull;

import org.onepf.openpush.OpenPushHelper;

/**
 * @author Kirill Rozov
 * @since 09.09.14.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        OpenPushHelper helper = OpenPushHelper.getInstance(context);
        if (isGCMRegistered(helper)) {
            final Settings settings = new Settings(context);
            final String newAndroidId = android.provider.Settings.Secure.ANDROID_ID;
            if (!newAndroidId.equals(settings.getAndroidId())) {
                settings.saveAndroidId(newAndroidId);
                helper.getProviderCallback().onNeedRetryRegister(GCMProvider.NAME);
            }
        }
    }

    private static boolean isGCMRegistered(@NonNull OpenPushHelper helper) {
        return helper.isRegistered()
                && helper.getCurrentProvider() != null
                && GCMProvider.NAME.equals(helper.getCurrentProvider().getName());
    }
}
