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
        }
        return Integer.MIN_VALUE;
    }

    public static boolean isSystemApp(Context context, String appPackage)
            throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(appPackage, 0);
        return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
    }

    private PackageUtils() {
    }
}
