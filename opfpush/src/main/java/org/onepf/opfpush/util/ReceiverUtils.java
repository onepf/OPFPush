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

package org.onepf.opfpush.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;

import org.onepf.opfpush.PackageChangeReceiver;
import org.onepf.opfpush.PushProvider;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import static org.onepf.opfpush.OPFConstants.ACTION_NO_AVAILABLE_PROVIDER;
import static org.onepf.opfpush.OPFConstants.ACTION_RECEIVE;
import static org.onepf.opfpush.OPFConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.OPFConstants.ACTION_UNREGISTRATION;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 07.09.14
 */
public final class ReceiverUtils {

    private static final String PACKAGE_DATA_SCHEME = "package";

    private ReceiverUtils() {
        throw new UnsupportedOperationException();
    }

    public static BroadcastReceiver registerPackageChangeReceiver(
            @NonNull final Context context,
            @NonNull final PushProvider provider
    ) {
        OPFLog.logMethod(context, provider);

        final PackageChangeReceiver packageChangeReceiver = new PackageChangeReceiver(provider);

        final IntentFilter appUpdateFilter = new IntentFilter(Intent.ACTION_PACKAGE_REPLACED);
        appUpdateFilter.addDataScheme(ReceiverUtils.PACKAGE_DATA_SCHEME);
        appUpdateFilter.addDataPath(context.getPackageName(), PatternMatcher.PATTERN_LITERAL);
        context.registerReceiver(packageChangeReceiver, appUpdateFilter);

        final String hostAppPackage = provider.getHostAppPackage();
        if (hostAppPackage != null) {
            OPFLog.d("Host app package isn't null");

            final IntentFilter hostAppRemovedFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            hostAppRemovedFilter.addDataScheme(ReceiverUtils.PACKAGE_DATA_SCHEME);
            hostAppRemovedFilter.addDataPath(hostAppPackage, PatternMatcher.PATTERN_LITERAL);
            context.registerReceiver(packageChangeReceiver, hostAppRemovedFilter);
        }

        return packageChangeReceiver;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static boolean isOPFReceiverRegistered(@NonNull final Context context) {
        OPFLog.logMethod(context);
        final Intent intentBroadcastReceive = new Intent(ACTION_RECEIVE);
        final Intent intentBroadcastRegistration = new Intent(ACTION_REGISTRATION);
        final Intent intentBroadcastUnregistration = new Intent(ACTION_UNREGISTRATION);
        final Intent intentBroadcastNoAvailableProvider = new Intent(ACTION_NO_AVAILABLE_PROVIDER);

        try {
            OPFChecks.checkReceiver(context, intentBroadcastReceive);
            OPFChecks.checkReceiver(context, intentBroadcastRegistration);
            OPFChecks.checkReceiver(context, intentBroadcastUnregistration);
            OPFChecks.checkReceiver(context, intentBroadcastNoAvailableProvider);
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }
}
