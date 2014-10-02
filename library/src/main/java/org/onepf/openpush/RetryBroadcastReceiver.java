package org.onepf.openpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.onepf.openpush.OpenPushHelper;

import static org.onepf.openpush.OpenPushLog.LOGD;

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
public final class RetryBroadcastReceiver extends BroadcastReceiver {

    public static final String EXTRA_PROVIDER_NAME = "provider_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGD("RetryBroadcastReceiver");
        final OpenPushHelper helper = OpenPushHelper.getInstance(context);
        if (helper.isInitDone() && intent.hasExtra(EXTRA_PROVIDER_NAME)) {
            helper.retryRegister(intent.getStringExtra(EXTRA_PROVIDER_NAME));
        }
    }
}
