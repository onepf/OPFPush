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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import org.onepf.opfpush.utils.NotificationUtils;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

/**
 * The default implementation of the {@link NotificationMaker} interface.
 * It shows a notification if the {@code bundle} contains an extra by the "opf_notification" key and it is "true" String.
 * It uses the {@link NotificationUtils#showNotification(Context, Bundle, NotificationPreparer)} method for showing a notification.
 *
 * @author Roman Savin
 * @since 23.06.2015
 */
public final class OPFNotificationMaker implements NotificationMaker {

    private static final String SHOW_NOTIFICATION_KEY = "opf_notification";

    @NonNull
    private NotificationPreparer notificationPreparer;

    /**
     * Default constructor. The {@link OPFNotificationPreparer} instance is used as {code notificationPreparer} by default.
     */
    public OPFNotificationMaker() {
        this(new OPFNotificationPreparer());
    }

    /**
     * Constructor with custom {@code notificationPreparer}.
     *
     * @param notificationPreparer The notification preparer which converts {@code bundle}
     *                             to a {@link org.onepf.opfpush.model.NotificationPayload} instance.
     *                             It can be useful for implementing custom converting from {@link Bundle}
     *                             to {@link org.onepf.opfpush.model.NotificationPayload}.
     */
    public OPFNotificationMaker(@NonNull final NotificationPreparer notificationPreparer) {
        this.notificationPreparer = notificationPreparer;
    }

    /**
     * Returns {@code true} if the {@code bundle} contains an extra by the "opf_notification" key and it is "true" String.
     *
     * @param bundle The bundle received from a push provider.
     * @return {@code true} if the {@code bundle} contains an extra by the "opf_notification" key and it is "true" String. False otherwise.
     */
    @Override
    public boolean needShowNotification(@NonNull final Bundle bundle) {
        OPFLog.logMethod(OPFUtils.toString(bundle));
        return Boolean.parseBoolean(bundle.getString(SHOW_NOTIFICATION_KEY));
    }

    /**
     * Shows a notification using the {@link NotificationUtils#showNotification(Context, Bundle, NotificationPreparer)} method.
     *
     * @param context The Context instance.
     * @param bundle  The bundle received from a push provider.
     */
    @Override
    public void showNotification(@NonNull final Context context, @NonNull final Bundle bundle) {
        OPFLog.logMethod(context, OPFUtils.toString(bundle));
        NotificationUtils.showNotification(context, bundle, notificationPreparer);
    }
}
