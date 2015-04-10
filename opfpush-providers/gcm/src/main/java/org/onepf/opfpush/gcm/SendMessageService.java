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

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.model.Message;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.io.IOException;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 10/13/14.
 */
public class SendMessageService extends IntentService {

    public static final String ACTION_SEND_MESSAGE = "org.onepf.opfpush.gcm.SEND_MSG";
    public static final String EXTRA_MESSAGE = "msg";
    public static final String EXTRA_MESSAGES_TO = "to";

    public SendMessageService() {
        super("SendMessageService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        OPFLog.logMethod(OPFUtils.toString(intent));

        if (ACTION_SEND_MESSAGE.equals(intent.getAction())) {
            final Message message = intent.getParcelableExtra(EXTRA_MESSAGE);
            final String messageTo = intent.getStringExtra(EXTRA_MESSAGES_TO);

            try {
                GoogleCloudMessaging.getInstance(this).send(
                        messageTo,
                        message.getId(),
                        message.getTimeToLeave(),
                        message.getData()
                );
                OPFLog.d("Message '%s' has sent.", message);
            } catch (IOException ex) {
                OPFLog.e(String.format("Error while send Message '%s'.", message), ex);
            }
        } else {
            OPFLog.w("Unknown action '%s'.", intent.getAction());
        }
    }
}
