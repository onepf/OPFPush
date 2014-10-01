package org.onepf.openpush.gcm;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGD("RetryBroadcastReceiver");
        if (new Settings(context).getState() == GCMProvider.STATE_NONE) {
            return;
        }

        final String action = intent.getAction();
        if (GCMConstants.ACTION_REGISTRATION_RETRY.equals(action)) {
            if (new Settings(context).getState() == GCMProvider.STATE_REGISTERING) {
                OpenPushHelper.getInstance(context).getProviderCallback().register(GCMProvider.NAME);
            }
        } else if (GCMConstants.ACTION_UNREGISTRATION_RETRY.equals(action)) {
            if (new Settings(context).getState() == GCMProvider.STATE_UNREGISTERING) {
                OpenPushHelper.getInstance(context).unregister();
            }
        } else {
            throw new UnsupportedOperationException(
                    String.format("Unknown action '%s'.", action));
        }
    }
}
