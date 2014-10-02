package org.onepf.openpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Kirill Rozov
 * @since 01.10.14.
 */
public final class RetryBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final OpenPushHelper helper = OpenPushHelper.getInstance(context);
        if (helper.isInitDone()) {
            final String action = intent.getAction();
            if (OpenPushConstants.ACTION_REGISTER.equals(action)) {
                helper.register(intent.getStringExtra(OpenPushConstants.EXTRA_PROVIDER_NAME));
            } else {
                throw new OpenPushException("Unknown action '%s'.", action);
            }
        }
    }
}
