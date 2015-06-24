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
 * TODO: add javadoc
 *
 * @author Roman Savin
 * @since 23.06.2015
 */
public final class OPFNotificationMaker implements NotificationMaker {

    private static final String SHOW_NOTIFICATION_KEY = "opf_notification";

    @NonNull
    private NotificationPreparer notificationPreparer;

    public OPFNotificationMaker() {
        this(new OPFNotificationPreparer());
    }

    public OPFNotificationMaker(@NonNull final NotificationPreparer notificationPreparer) {
        this.notificationPreparer = notificationPreparer;
    }

    @Override
    public boolean needShowNotification(@NonNull final Bundle bundle) {
        OPFLog.logMethod(OPFUtils.toString(bundle));
        return Boolean.parseBoolean(bundle.getString(SHOW_NOTIFICATION_KEY));
    }

    @Override
    public void showNotification(@NonNull final Context context, @NonNull final Bundle bundle) {
        OPFLog.logMethod(context, OPFUtils.toString(bundle));
        NotificationUtils.showNotification(context, bundle, notificationPreparer);
    }
}
