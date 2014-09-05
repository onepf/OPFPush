package org.onepf.openpush.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.openpush.OpenPushHelper;

public class GcmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            final String messageType = gcm.getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                OpenPushHelper.sendMessage(context, GoogleCloudMessagingProvider.NAME, new Bundle(intentExtras));
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                OpenPushHelper.sendMessageDeleted(context, GoogleCloudMessagingProvider.NAME, new Bundle(intentExtras));
            }
        }
    }
}
