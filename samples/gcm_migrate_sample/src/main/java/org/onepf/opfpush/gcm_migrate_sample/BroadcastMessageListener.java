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
