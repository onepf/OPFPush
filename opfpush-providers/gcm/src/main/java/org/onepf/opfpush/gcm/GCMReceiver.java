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

package org.onepf.opfpush.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfutils.OPFUtils;

/**
 * Receiver for events from Google Cloud Messaging and {@link GCMProvider}.
 *
 * @author Roman Savin
 */
public class GCMReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFPushLog.methodD(GCMReceiver.class, "onReceive", context, OPFUtils.toString(intent));

        if (GCMConstants.ACTION_REGISTRATION.equals(intent.getAction())) {
            final Bundle extras = intent.getExtras();
            if (extras != null
                    && extras.size() == 1
                    && extras.containsKey(GCMConstants.EXTRA_REGISTRATION_ID)) {
                intent.setAction(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            } else {
                return;
            }
        }

        intent.setComponent(new ComponentName(context, GCMService.class));
        startWakefulService(context, intent);
        if (isOrderedBroadcast()) {
            setResultCode(Activity.RESULT_OK);
        }
    }
}
