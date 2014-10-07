package org.onepf.opfpush;

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
        final OPFPushHelper helper = OPFPushHelper.getInstance(context);
        if (helper.isInitDone()) {
            final String action = intent.getAction();
            if (Constants.ACTION_REGISTER.equals(action)) {
                helper.register(intent.getStringExtra(Constants.EXTRA_PROVIDER_NAME));
            } else {
                throw new OPFPushException("Unknown action '%s'.", action);
            }
        }
    }
}
