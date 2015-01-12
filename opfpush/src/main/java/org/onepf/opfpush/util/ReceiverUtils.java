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
import android.content.pm.ResolveInfo;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;

import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.PackageChangeReceiver;
import org.onepf.opfpush.PushProvider;

import java.util.List;

import static android.content.pm.PackageManager.GET_RESOLVED_FILTER;
import static org.onepf.opfpush.OPFConstants.ACTION_NO_AVAILABLE_PROVIDER;
import static org.onepf.opfpush.OPFConstants.ACTION_RECEIVE;
import static org.onepf.opfpush.OPFConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.OPFConstants.ACTION_UNREGISTRATION;

/**
 * Different utils for check info about installed packages on device.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 07.09.14
 */
public final class ReceiverUtils {

    private static final String PACKAGE_DATA_SCHEME = "package";

    private ReceiverUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Register {@code BroadcastReceiver} for listen changes associated with {@code PushProvider}.
     * Listen update of current application and remove host application package.
     *
     * @param context  The current context.
     * @param provider Provider for what listen package changes.
     * @return Created {@code BroadcastReceiver}.
     */
    public static BroadcastReceiver registerPackageChangeReceiver(
            @NonNull final Context context,
            @NonNull final PushProvider provider
    ) {
        OPFPushLog.methodD(ReceiverUtils.class, "registerPackageChangeReceiver", context, provider);

        final PackageChangeReceiver packageChangeReceiver = new PackageChangeReceiver(provider);

        final IntentFilter appUpdateFilter = new IntentFilter(Intent.ACTION_PACKAGE_REPLACED);
        appUpdateFilter.addDataScheme(ReceiverUtils.PACKAGE_DATA_SCHEME);
        appUpdateFilter.addDataPath(context.getPackageName(), PatternMatcher.PATTERN_LITERAL);
        context.registerReceiver(packageChangeReceiver, appUpdateFilter);

        final String hostAppPackage = provider.getHostAppPackage();
        if (hostAppPackage != null) {
            OPFPushLog.d("Host app package isn't null");

            final IntentFilter hostAppRemovedFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            hostAppRemovedFilter.addDataScheme(ReceiverUtils.PACKAGE_DATA_SCHEME);
            hostAppRemovedFilter.addDataPath(hostAppPackage, PatternMatcher.PATTERN_LITERAL);
            context.registerReceiver(packageChangeReceiver, hostAppRemovedFilter);
        }

        return packageChangeReceiver;
    }

    public static boolean isOPFReceiverRegistered(@NonNull final Context context) {
        OPFPushLog.methodD(ReceiverUtils.class, "isOPFReceiverRegistered", context);

        final Intent intent = new Intent(ACTION_RECEIVE);
        final List<ResolveInfo> resolveInfos = context.getPackageManager()
                .queryBroadcastReceivers(intent, GET_RESOLVED_FILTER);

        for (ResolveInfo resolveInfo : resolveInfos) {
            final IntentFilter intentFilter = resolveInfo.filter;
            if (intentFilter != null && intentFilter.hasAction(ACTION_RECEIVE)
                    && intentFilter.hasAction(ACTION_REGISTRATION)
                    && intentFilter.hasAction(ACTION_UNREGISTRATION)
                    && intentFilter.hasAction(ACTION_NO_AVAILABLE_PROVIDER)) {
                return true;
            }
        }

        return false;
    }
}
