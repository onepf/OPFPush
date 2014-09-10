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

package org.onepf.openpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.onepf.openpush.util.PackageUtils;

public class PackageChangeReceiver extends BroadcastReceiver {

    @NotNull
    private PushProvider mProvider;

    PackageChangeReceiver(@NotNull PushProvider provider) {
        mProvider = provider;
    }

    @Override
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            if (mProvider.getHostAppPackage().equals(getAppPackage(intent))) {
                OpenPushHelper.getInstance(context).onHostAppRemoved(mProvider);
            }
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            if (context.getPackageName().equals(getAppPackage(intent))) {
                OpenPushHelper.getInstance(context).onNeedRetryRegister(mProvider.getName());
            }
        }
    }

    private static String getAppPackage(Intent intent) {
        return intent.getDataString().replace(PackageUtils.PACKAGE_DATA_SCHEME, "").substring(1);
    }
}
