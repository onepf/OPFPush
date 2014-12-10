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

package org.onepf.opfpush.pushsample.listener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.model.State;
import org.onepf.opfpush.pushsample.R;
import org.onepf.opfpush.pushsample.activity.DemoActivity;
import org.onepf.opfpush.pushsample.model.MessageEvent;
import org.onepf.opfpush.pushsample.model.RegisteredEvent;
import org.onepf.opfpush.pushsample.model.UnregisteredEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

import static org.onepf.opfpush.pushsample.util.Constants.MESSAGE_EXTRA_KEY;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class DemoEventListener implements EventListener {

    private static final String TAG = DemoEventListener.class.getSimpleName();

    private static final AtomicInteger NOTIFICATION_ID = new AtomicInteger(0);

    @NonNull
    private Context appContext;

    @NonNull
    private NotificationManager notificationManager;

    public DemoEventListener(@NonNull final Context context) {
        this.appContext = context.getApplicationContext();
        this.notificationManager = (NotificationManager) appContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onMessage(@NonNull String providerName, @Nullable Bundle extras) {
        Log.d(TAG, String.format("onMessage(%1$s, %2$s)", providerName, extras));
        if (extras == null) {
            return;
        }

        final String message = extras.getString(MESSAGE_EXTRA_KEY);
        if (message != null) {
            try {
                showNotification(
                        appContext.getString(R.string.message_notification_title),
                        message
                );
                EventBus.getDefault().postSticky(new MessageEvent(URLDecoder.decode(message, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {
        Log.d(TAG, String.format("onDeletedMessages(%1$s, %2$s)", providerName, messagesCount));
    }

    @Override
    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
        Log.d(TAG, String.format("onRegistered(%1$s, %2$s)", providerName, registrationId));
        EventBus.getDefault().postSticky(new RegisteredEvent(registrationId));
    }

    @Override
    public void onUnregistered(@NonNull String providerName, @NonNull String registrationId) {
        Log.d(TAG, String.format("onUnregistered(%1$s, %2$s)", providerName, registrationId));
        EventBus.getDefault().postSticky(new UnregisteredEvent(registrationId));
    }

    @Override
    public void onRegistrationError(@NonNull String providerName, @NonNull OPFError error) {
        Log.d(TAG, String.format("onRegistrationError(%1$s, %2$s)", providerName, error));
    }

    @Override
    public void onUnregistrationError(@NonNull String providerName, @NonNull OPFError error) {
        Log.d(TAG, String.format("onUnregistrationError(%1$s, %2$s)", providerName, error));
    }

    @Override
    public void onRegistrationStateError(@NonNull String providerName, @NonNull State state) {
        Log.d(TAG, String.format("onRegistrationStateError(%1$s, %2$s)", providerName, state));
    }

    @Override
    public void onUnregistrationStateError(@NonNull String providerName, @NonNull State state) {
        Log.d(TAG, String.format("onUnregistrationStateError(%1$s, %2$s)", providerName, state));
    }

    @Override
    public void onNoAvailableProvider() {
        Log.d(TAG, "onNoAvailableProvider()");
    }

    @Override
    public void onWrongStateError(@NonNull String providerName, @NonNull OPFError error, @NonNull State state) {
        Log.d(TAG, String.format("onWrongStateError(%1$s, %2$s, %3$s)", providerName, error, state));
    }

    @Override
    public void onProviderBecameUnavailable(@NonNull String providerName) {
        Log.d(TAG, String.format("onProviderBecameUnavailable(%s)", providerName));
    }

    private void showNotification(
            @NonNull final String notificationTitle,
            @NonNull final String notificationText) {

        final PendingIntent pendingIntent = PendingIntent.getActivity(
                appContext,
                0,
                new Intent(appContext, DemoActivity.class),
                0
        );

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(appContext)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID.getAndIncrement(), notificationBuilder.build());
    }
}
