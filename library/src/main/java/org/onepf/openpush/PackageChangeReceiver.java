package org.onepf.openpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.onepf.openpush.util.PackageUtils;

public class PackageChangeReceiver extends BroadcastReceiver {

    @NotNull
    private PushProvider mProvider;

    public PackageChangeReceiver(@NotNull PushProvider provider) {
        Assert.assertNotNull(provider);
        mProvider = provider;
    }

    @Override
    public void onReceive(@NotNull Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)
                && mProvider.getHostAppPackage().equals(getAppPackage(intent))) {
            OpenPushHelper.getInstance(context).onHostAppRemoved(mProvider);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            OpenPushHelper.getInstance(context).onProviderNeedUpdate(mProvider.getName());
        }
    }

    private static String getAppPackage(Intent intent) {
        return intent.getDataString().replace(PackageUtils.PACKAGE_DATA_SCHEME + ':', "");
    }
}
