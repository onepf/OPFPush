package org.onepf.opfpush.gcm_migrate_sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.MessageListener;

/**
 * @author Kirill Rozov
 * @since 10/7/14.
 */
public class BroadcastMessageListener implements MessageListener {

    public static final String EXTRA_PROVIDER_NAME = "provider_name";
    public static final String EXTRA_MESSAGES_COUNT = "messages_count";

    public static final String ACTION_MESSAGE_RECEIVE = "org.onepf.opfpush.MESSAGE_RECEIVE";
    public static final String ACTION_MESSAGES_DELETED = "org.onepf.opfpush.MESSAGED_DELETED";

    private final Context mAppContext;

    public BroadcastMessageListener(@NonNull Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public void onMessageReceive(@NonNull String providerName, @Nullable Bundle extras) {
        Intent intent = new Intent(ACTION_MESSAGE_RECEIVE);
        intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        if (extras != null) {
            intent.putExtras(extras);
        }
        mAppContext.sendBroadcast(intent);
    }

    @Override
    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {
        Intent intent = new Intent(ACTION_MESSAGES_DELETED);
        intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        intent.putExtra(EXTRA_MESSAGES_COUNT, messagesCount);
        mAppContext.sendBroadcast(intent);
    }
}
