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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.onepf.opfutils.OPFLog;

import static android.provider.Settings.Secure;
import static android.provider.Settings.Secure.ANDROID_ID;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 09.09.14.
 */
public final class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFLog.logMethod(context, intent);

        final OPFPushHelper helper = OPFPush.getHelper();
        if (helper.isRegistered()) {
            OPFLog.i("Helper is registered");
            if (isAndroidIDChanged(context)) {
                OPFLog.i("Android ID changed.");
                helper.onNeedRetryRegister();
            } else {
                OPFLog.i("Android ID hasn't been changed");
                helper.registerPackageChangeReceiver();
            }
        } else if (helper.isRegistering()) {
            OPFLog.i("Registration in progress. Retry register after reboot.");
            helper.restartRegisterOnBoot();
        }
    }

    private boolean isAndroidIDChanged(@NonNull final Context context) {
        return !Secure.getString(context.getContentResolver(), ANDROID_ID)
                .equals(Settings.getInstance(context).getLastAndroidId());
    }
}
