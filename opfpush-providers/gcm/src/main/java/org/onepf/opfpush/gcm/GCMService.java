/*
 * Copyright 2012-2015 One Platform Foundation
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

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.google.android.gms.gcm.GcmListenerService;
import org.onepf.opfpush.OPFConstants;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.gcm.GCMConstants.PROVIDER_NAME;

/**
 * @author Roman Savin
 * @since 16.06.2015
 */
public class GCMService extends GcmListenerService {

    @Override
    public void onMessageReceived(@NonNull final String from, @NonNull final Bundle data) {
        OPFLog.logMethod(from, OPFUtils.toString(data));
        OPFPush.getHelper().getReceivedMessageHandler().onMessage(PROVIDER_NAME, data);
    }

    @Override
    public void onDeletedMessages() {
        OPFLog.logMethod();
        OPFPush.getHelper().getReceivedMessageHandler()
                .onDeletedMessages(PROVIDER_NAME, OPFConstants.MESSAGES_COUNT_UNKNOWN);
    }
}
