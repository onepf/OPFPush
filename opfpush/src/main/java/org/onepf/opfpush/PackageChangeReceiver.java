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
import android.support.annotation.Nullable;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

/**
 * @author Roman Savin
 * @since 17.12.14
 */
public final class PackageChangeReceiver extends BroadcastReceiver {

    private static final String PACKAGE_URI_PREFIX = "package:";

    @NonNull
    private final PushProvider provider;

    public PackageChangeReceiver(@NonNull final PushProvider provider) {
        super();
        this.provider = provider;
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFLog.logMethod(context, OPFUtils.toString(intent));

        final OPFPushHelper helper = OPFPush.getHelper();
        final String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            final String hostAppPackage = provider.getHostAppPackage();
            if (hostAppPackage != null && hostAppPackage.equals(getAppPackage(intent))) {
                OPFLog.i("Host app '%s' of provider '%s' has been removed.",
                        hostAppPackage, provider.getName());
                helper.registerNextAvailableProvider(provider.getName());
            }
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)
                && context.getPackageName().equals(getAppPackage(intent))) {
            OPFLog.d("Application updated.");
            helper.onNeedRetryRegister();
        }
    }

    @Nullable
    private String getAppPackage(@NonNull final Intent intent) {
        final String data = intent.getDataString();
        return data.startsWith(PACKAGE_URI_PREFIX)
                ? data.replaceFirst(PACKAGE_URI_PREFIX, "") : null;
    }
}
