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
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import static org.onepf.opfpush.OPFPushLog.LOGI;

/**
 * Different utils for check info about installed packages on device.
 *
 * @author Kirill Rozov
 * @since 07.09.14
 */
public final class PackageUtils {
    public static final String PACKAGE_DATA_SCHEME = "package";

    /**
     * Get version code of current application.
     *
     * @return If find app - return it's version code, else {@link Integer#MIN_VALUE}.
     */
    public static int getAppVersion(@NonNull Context context)
            throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        if (packageInfo == null) {
            throw new PackageManager.NameNotFoundException(context.getPackageName());
        }
        return packageInfo.versionCode;
    }

    /**
     * Check is application system.
     *
     * @param context    The current context.
     * @param appPackage Package of application for verify.
     * @return True when application is system, false - otherwise.
     */
    public static boolean isSystemApp(@NonNull Context context, @NonNull String appPackage) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(appPackage, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    || (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Check is application installed on device.
     *
     * @param context    The current context.
     * @param appPackage Package of application for verify.
     * @return True when application is installed, false - otherwise.
     */
    public static boolean isInstalled(@NonNull Context context, @NonNull String appPackage) {
        try {
            return context.getPackageManager().getApplicationInfo(appPackage, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Register {@code BroadcastReceiver} for listen changes associated with {@code PushProvider}.
     * Listen update of current application and remove host application package.
     *
     * @param context  The current context.
     * @param provider Provider for what listen package changes.
     * @return Created {@code BroadcastReceiver}.
     */
    public static BroadcastReceiver registerPackageChangeReceiver(@NonNull Context context,
                                                                  @NonNull PushProvider provider) {
        final PackageChangeReceiver mPackageReceiver = new PackageChangeReceiver(provider);

        final IntentFilter appUpdateFilter = new IntentFilter(Intent.ACTION_PACKAGE_REPLACED);
        appUpdateFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
        appUpdateFilter.addDataPath(context.getPackageName(), PatternMatcher.PATTERN_LITERAL);
        context.registerReceiver(mPackageReceiver, appUpdateFilter);

        String hostAppPackage = provider.getHostAppPackage();
        if (hostAppPackage != null) {
            final IntentFilter hostAppRemovedFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            hostAppRemovedFilter.addDataScheme(PackageUtils.PACKAGE_DATA_SCHEME);
            hostAppRemovedFilter.addDataPath(hostAppPackage, PatternMatcher.PATTERN_LITERAL);
            context.registerReceiver(mPackageReceiver, hostAppRemovedFilter);
        }

        return mPackageReceiver;
    }

    private PackageUtils() {
    }

    private static class PackageChangeReceiver extends BroadcastReceiver {

        private static final String PACKAGE_URI_PREFIX = PACKAGE_DATA_SCHEME + ':';

        @NonNull
        private PushProvider mProvider;

        PackageChangeReceiver(@NonNull PushProvider provider) {
            mProvider = provider;
        }

        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                String hostAppPackage = mProvider.getHostAppPackage();
                Assert.assertNotNull(hostAppPackage);
                if (hostAppPackage.equals(getAppPackage(intent))) {
                    LOGI("Host app '%s' of provider '%s' removed.",
                            hostAppPackage, mProvider.getName());
                    OPFPushHelper.getInstance(context).onProviderUnavailable(mProvider);
                }
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                if (context.getPackageName().equals(getAppPackage(intent))) {
                    LOGI("Application updated.");
                    OPFPushHelper.getInstance(context).onNeedRetryRegister();
                }
            }
        }

        @Nullable
        private static String getAppPackage(Intent intent) {
            final String data = intent.getDataString();
            return data.startsWith(PACKAGE_URI_PREFIX)
                    ? data.replaceFirst(PACKAGE_URI_PREFIX, "") : null;
        }
    }
}
