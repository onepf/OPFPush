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

package org.onepf.openpush.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.jetbrains.annotations.NotNull;

/**
 * Created by krozov on 07.09.14.
 */
public final class PackageUtils {
    public static final String PACKAGE_DATA_SCHEME = "package";

    /**
     * Get version code of current application.
     *
     * @return If find app - return it's version code, else {@link Integer#MIN_VALUE}.
     */
    public static int getAppVersion(@NotNull Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo == null ? Integer.MIN_VALUE : packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return Integer.MIN_VALUE;
        }
    }

    public static boolean isSystemApp(@NotNull Context context, @NotNull String appPackage) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(appPackage, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isInstalled(@NotNull Context context, @NotNull String appPackage) {
        try {
            return context.getPackageManager().getApplicationInfo(appPackage, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private PackageUtils() {
    }
}
