/*
 * Copyright 2012-2015 One Platform Foundation
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

import org.onepf.opfpush.backoff.RetryManager;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

/**
 * @author Roman Savin
 * @since 17.12.14
 */
public final class PackageChangeReceiver extends BroadcastReceiver {

    private static final String PACKAGE_URI_PREFIX = "package:";

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFLog.logMethod(context, OPFUtils.toString(intent));

        final OPFPushHelper helper = OPFPush.getHelper();
        final String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            final String providerName = helper.getProviderNameByHostApp(getAppPackage(intent));
            if (providerName == null) {
                return;
            }
            checkCurrentProvider(providerName);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)
                && context.getPackageName().equals(getAppPackage(intent))) {
            OPFLog.d("Application updated.");
            helper.onNeedRetryRegister();
        }
    }

    private void checkCurrentProvider(@NonNull final String providerName) {
        final OPFPushHelper helper = OPFPush.getHelper();
        final PushProvider currentProvider = helper.getCurrentProvider();
        if (currentProvider == null) {
            return;
        }

        final String currentProviderName = currentProvider.getName();
        if (providerName.equals(currentProviderName)) {
            OPFLog.i("Host app of provider '%s' has been removed.", currentProviderName);
            clearSettingsForProvider(currentProviderName);
            RetryManager.getInstance().cancelRetryAllOperations(providerName);
            helper.registerNextAvailableProvider(currentProviderName);
        }
    }

    private void clearSettingsForProvider(@NonNull final String providerName) {
        final Settings settings = OPFPush.getHelper().getSettings();
        settings.removeRegisteringProvider(providerName);
        settings.removeUnregisteringProvider(providerName);
    }

    @Nullable
    private String getAppPackage(@NonNull final Intent intent) {
        final String data = intent.getDataString();
        return data.startsWith(PACKAGE_URI_PREFIX)
                ? data.replaceFirst(PACKAGE_URI_PREFIX, "") : null;
    }
}
