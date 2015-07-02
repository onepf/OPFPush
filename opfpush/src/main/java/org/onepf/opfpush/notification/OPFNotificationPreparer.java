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

package org.onepf.opfpush.notification;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import org.onepf.opfpush.model.NotificationPayload;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

/**
 * The default implementation of the {@link NotificationPreparer} interface.
 * It uses the same keys as GCM downstream messages parameters.
 * https://developers.google.com/cloud-messaging/server-ref#notification-payload-support
 *
 * @author Roman Savin
 * @since 23.06.2015
 */
public final class OPFNotificationPreparer implements NotificationPreparer {

    private static final String TITLE = "title";
    private static final String ICON = "icon";
    private static final String BODY = "body";
    private static final String SOUND = "sound";
    private static final String TAG = "tag";
    private static final String COLOR = "color";
    private static final String CLICK_ACTION = "click_action";

    @Nullable
    @Override
    public NotificationPayload prepare(@NonNull final Bundle bundle) {
        OPFLog.logMethod(OPFUtils.toString(bundle));
        final String title = bundle.getString(TITLE);
        if (TextUtils.isEmpty(title)) {
            OPFLog.i("Notification title is empty. Notification will not be shown");
            return null;
        }

        final String icon = bundle.getString(ICON);
        if (TextUtils.isEmpty(icon)) {
            OPFLog.i("Notification icon is empty. Notification will not be shown");
            return null;
        }

        final NotificationPayload.Builder builder = new NotificationPayload.Builder()
                .setTitle(title)
                .setIcon(icon)
                .setBody(bundle.getString(BODY))
                .setSound(bundle.getString(SOUND))
                .setTag(bundle.getString(TAG))
                .setColor(bundle.getString(COLOR))
                .setClickAction(bundle.getString(CLICK_ACTION));


        return builder.build();
    }
}
