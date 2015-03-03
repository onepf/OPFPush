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
import android.support.annotation.NonNull;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

/**
 * Forward Google Cloud Messaging (GCM) messages to the {@link GCMService}.
 *
 * @author Roman Savin
 */
public class GCMReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFLog.methodD(context, OPFUtils.toString(intent));

        intent.setComponent(new ComponentName(context, GCMService.class));
        startWakefulService(context, intent);
        if (isOrderedBroadcast()) {
            setResultCode(Activity.RESULT_OK);
        }
    }
}
