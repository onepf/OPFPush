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

package org.onepf.opfpush.gcm_migrate_sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.MessageListener;

/**
 * @author Kirill Rozov
 * @since 10/7/14.
 */
public class BroadcastMessageListener implements MessageListener {

    public static final String EXTRA_PROVIDER_NAME = "provider_name";
    public static final String EXTRA_MESSAGES_COUNT = "messages_count";

    private static final String ACTION_MESSAGE = "org.onepf.opfpush.intent.RECEIVE";

    private final Context mAppContext;

    public BroadcastMessageListener(@NonNull Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static String getMessageType(@NonNull Intent intent) {
        if (intent.hasExtra(EXTRA_MESSAGES_COUNT)) {
            return GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE;
        } else if (intent.getExtras() != null) {
            return GoogleCloudMessaging.MESSAGE_TYPE_DELETED;
        } else {
            return null;
        }
    }

    @Override
    public void onMessageReceive(@NonNull String providerName, @Nullable Bundle extras) {
        Intent intent = new Intent(ACTION_MESSAGE);
        intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        if (extras != null) {
            intent.putExtras(extras);
        }
        mAppContext.sendBroadcast(intent);
    }

    @Override
    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {
        Intent intent = new Intent(ACTION_MESSAGE);
        intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        intent.putExtra(EXTRA_MESSAGES_COUNT, messagesCount);
        mAppContext.sendBroadcast(intent);
    }
}
