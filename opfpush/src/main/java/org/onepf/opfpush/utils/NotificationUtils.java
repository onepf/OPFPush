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

package org.onepf.opfpush.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import org.onepf.opfpush.model.NotificationPayload;
import org.onepf.opfpush.notification.NotificationPreparer;
import org.onepf.opfpush.notification.OPFNotificationPreparer;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.regex.Pattern;

import static android.app.Notification.DEFAULT_SOUND;
import static android.content.Context.NOTIFICATION_SERVICE;
import static java.util.Locale.US;

/**
 * TODO: javadoc
 *
 * @author Roman Savin
 * @since 23.06.2015
 */
public final class NotificationUtils {

    private static final String HEX_COLOR_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
    private static final Pattern COLOR_PATTERN = Pattern.compile(HEX_COLOR_PATTERN);

    private static final String SOUND_DEFAULT_VALUE = "default";

    private static final String DEFAULT_TAG_PREFIX = "OPF-Notification:";

    private NotificationUtils() {
        throw new UnsupportedOperationException();
    }

    public static void showNotification(@NonNull final Context context,
                                        @NonNull final Bundle bundle) {
        showNotification(context, bundle, new OPFNotificationPreparer());
    }

    public static void showNotification(@NonNull final Context context,
                                        @NonNull final Bundle bundle,
                                        @NonNull final NotificationPreparer notificationPreparer) {
        OPFLog.logMethod(context, OPFUtils.toString(bundle), notificationPreparer);
        final NotificationPayload notificationPayload = notificationPreparer.prepare(bundle);
        if (notificationPayload == null) {
            return;
        }

        final String icon = notificationPayload.getIcon();
        final int iconDrawableId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());

        if (iconDrawableId == 0) {
            OPFLog.i(String.format(US, "Can't find drawable with name : \"%s\". Notification will not be shown", icon));
            return;
        }

        final Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setContentTitle(notificationPayload.getTitle())
                .setSmallIcon(iconDrawableId)
                .setContentText(notificationPayload.getBody());

        final String sound = notificationPayload.getSound();
        if (!TextUtils.isEmpty(sound)) {
            safeSetSound(notificationBuilder, sound);
        }

        final String color = notificationPayload.getColor();
        if (!TextUtils.isEmpty(color)) {
            safeSetColor(notificationBuilder, color);
        }

        final String clickAction = notificationPayload.getClickAction();
        if (!TextUtils.isEmpty(clickAction)) {
            setContentIntent(notificationBuilder, context, clickAction, bundle);
        }

        final String tag = TextUtils.isEmpty(notificationPayload.getTag())
                ? DEFAULT_TAG_PREFIX + System.currentTimeMillis()
                : notificationPayload.getTag();
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(tag, 0, buildNotification(notificationBuilder));
    }

    private static void safeSetSound(@NonNull final Notification.Builder notificationBuilder,
                                     @NonNull final String sound) {
        if (SOUND_DEFAULT_VALUE.equals(sound)) {
            notificationBuilder.setDefaults(DEFAULT_SOUND);
        } else {
            OPFLog.i(String.format(US, "Sound \"%s\" is not supported. Only \"default\" sound is supported currently.", sound));
        }
    }

    @SuppressLint("NewApi")
    private static void safeSetColor(@NonNull final Notification.Builder notificationBuilder,
                                     @NonNull final String color) {
        //todo: check on not lolipop devices
        if (!COLOR_PATTERN.matcher(color).matches()) {
            OPFLog.i(String.format(US, "Color \"%s\" doesn't match to #rrggbb format.", color));
        } else if (!isSetColorSupported()) {
            OPFLog.i("Notification.Builder.setColor() is not supported");
        } else {
            notificationBuilder.setColor(Color.parseColor(color));
        }
    }

    private static void setContentIntent(@NonNull final Notification.Builder notificationBuilder,
                                         @NonNull final Context context,
                                         @NonNull final String clickAction,
                                         @NonNull final Bundle bundle) {
        final Intent intent = new Intent(clickAction);
        intent.setPackage(context.getPackageName());
        intent.putExtras(bundle);

        notificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
    }

    private static boolean isSetColorSupported() {
        return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private static Notification buildNotification(@NonNull final Notification.Builder builder) {
        return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN ? builder.build() : builder.getNotification();
    }
}
