/*******************************************************************************
 * Copyright 2014 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

package org.onepf.openpush.sample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.onepf.openpush.util.LogUtils;
import org.onepf.openpush.PushListener;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class SimplePushListener extends PushListener {

    private static final String TAG = SimplePushListener.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;

    public static final String ONEPF_ACTION_REGISTERED = "org.onepf.openpush.sample.action.REGISTERED";
    public static final String ONEPF_ACTION_UNREGISTERED = "org.onepf.openpush.sample.action.UNREGISTERED";
    public static final String ONEPF_ACTION_MESSAGE = "org.onepf.openpush.sample.action.MESSAGE";
    public static final String ONEPF_ACTION_ERROR = "org.onepf.openpush.sample.action.ERROR";

    public static final String ONEPF_EXTRA_MESSAGE = "org.onepf.openpush.sample.extra.MESSAGE";
    public static final String ONEPF_EXTRA_PROVIDER = "org.onepf.openpush.sample.extra.PROVIDER";

    @Override
    public void onRegistered(Context context, String token, String providerName) {
        sendMessageToApplication(context, token, providerName, new Intent(ONEPF_ACTION_REGISTERED));
    }

    @Override
    public void onUnregistered(Context context, String token, String providerName) {
        sendMessageToApplication(context, token, providerName, new Intent(ONEPF_ACTION_UNREGISTERED));
    }

    @Override
    public void onError(Context context, String errorMessage, String providerName) {
        sendMessageToApplication(context, errorMessage, providerName, new Intent(ONEPF_ACTION_ERROR));
    }

    @Override
    public void onMessage(Context context, Bundle extras, String providerName) {
        Intent intent = new Intent(ONEPF_ACTION_MESSAGE);
        intent.putExtra(ONEPF_EXTRA_MESSAGE, extras);
        sendIntentToApplication(context, providerName, intent);

        sendNotification(context, extras, providerName);
    }

    private void sendMessageToApplication(Context context, String message, String providerName, Intent intent) {
        intent.putExtra(ONEPF_EXTRA_MESSAGE, message);
        sendIntentToApplication(context, providerName, intent);
    }

    private void sendIntentToApplication(Context context, String providerName, Intent intent) {
        intent.putExtra(ONEPF_EXTRA_PROVIDER, providerName);

        Log.d(TAG, "Sending intent to app: \n" + LogUtils.intentToString(intent));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendNotification(Context context, Bundle data, String providerName) {
        NotificationManager mNotificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent activityIntent = new Intent(context, PushSampleActivity.class);
        activityIntent.setAction(ONEPF_ACTION_MESSAGE);
        activityIntent.putExtra(ONEPF_EXTRA_PROVIDER, providerName);
        activityIntent.putExtra(ONEPF_EXTRA_MESSAGE, data);

        Log.d(TAG, "Sending pending intent: \n" + LogUtils.intentToString(activityIntent));

        PendingIntent contentIntent
                = PendingIntent.getActivity(context, NOTIFICATION_ID, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_push_notification)
                        .setContentTitle(providerName +  " notification")
                        .setContentText(data.toString());

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
