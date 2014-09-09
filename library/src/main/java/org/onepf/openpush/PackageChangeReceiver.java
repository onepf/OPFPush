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
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            OpenPushHelper openPushHelper = OpenPushHelper.getInstance(context);
            String removedAppPackage = intent.getDataString()
                    .replace(PackageUtils.PACKAGE_DATA_SCHEME + ':', "");
            if (mProvider.getHostAppPackage().equals(removedAppPackage)
                    && openPushHelper.getInitStatus() == OpenPushHelper.INIT_SUCCESS)
                openPushHelper.onHostAppRemoved(mProvider);
        }
    }
}
