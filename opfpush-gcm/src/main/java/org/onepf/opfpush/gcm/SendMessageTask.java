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

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.Message;

import java.io.IOException;

import static org.onepf.opfpush.OPFPushLog.LOGE;
import static org.onepf.opfpush.OPFPushLog.LOGI;

/**
 * Task for send message from client to server.
 *
 * @author Kirill Rozov
 * @since 22.09.14.
 */
class SendMessageTask extends AsyncTask<Void, Void, Void> {
    private static final String GCM_SENDER_SUFFIX = "@gcm.googleapis.com";

    private final String mTo;
    private final Message mMessage;
    private final Context mContext;

    SendMessageTask(@NonNull Context context,
                    @NonNull String senderId,
                    @NonNull Message message) {
        mTo = senderId + GCM_SENDER_SUFFIX;
        mMessage = message;
        mContext = context.getApplicationContext();
    }

    protected Void doInBackground(Void... params) {
        try {
            if (!isCancelled()) {
                GoogleCloudMessaging.getInstance(mContext)
                        .send(mTo, mMessage.getId(), mMessage.getTimeToLeave(), mMessage.getData());
                LOGI(String.format("Message '%s' has sent.", mMessage));
            }
        } catch (IOException ex) {
            LOGE(String.format("Error while send Message '%s'.", mMessage), ex);
        }
        return null;
    }
}
