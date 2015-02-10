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
import android.support.annotation.NonNull;

import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.OPFConstants.ACTION_RETRY_REGISTER;
import static org.onepf.opfpush.OPFConstants.ACTION_RETRY_UNREGISTER;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 01.10.14.
 */
public final class RetryBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFLog.methodD(RetryBroadcastReceiver.class, "onReceive",
                context, OPFUtils.toString(intent));

        final OPFPushHelper helper = OPFPush.getHelper();
        if (helper.isInitDone()) {
            OPFLog.d("Initialisation is done");

            final String action = intent.getAction();
            switch (action) {
                case ACTION_RETRY_REGISTER:
                    helper.register(intent.getStringExtra(EXTRA_PROVIDER_NAME));
                    break;
                case ACTION_RETRY_UNREGISTER:
                    helper.unregister(intent.getStringExtra(EXTRA_PROVIDER_NAME));
                    break;
                default:
                    throw new OPFPushException("Unknown action '%s'.", action);
            }
        }
    }
}
