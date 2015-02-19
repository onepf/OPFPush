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

package org.onepf.opfpush.pushsample.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfpush.pushsample.R;
import org.onepf.opfpush.pushsample.activity.DemoActivity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Roman Savin
 * @since 25.12.14
 */
public final class NotificationUtils {

    private static final AtomicInteger NOTIFICATION_ID = new AtomicInteger(0);

    private NotificationUtils() {
        throw new UnsupportedOperationException();
    }

    public static void showNotification(@NonNull final Context context,
                                        @NonNull final String notificationTitle,
                                        @NonNull final String notificationText) {
        OPFLog.methodD(context, notificationTitle, notificationText);


        final PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, DemoActivity.class),
                0
        );

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentIntent(pendingIntent);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID.getAndIncrement(), notificationBuilder.build());
    }
}
