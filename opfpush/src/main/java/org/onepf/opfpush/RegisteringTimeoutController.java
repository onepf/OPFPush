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

package org.onepf.opfpush;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.onepf.opfutils.OPFLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.onepf.opfpush.OPFConstants.ACTION_CHECK_REGISTERING_TIMEOUT;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;

/**
 * @author Roman Savin
 * @since 18.02.2015
 */
final class RegisteringTimeoutController {

    private static final long TIMEOUT_MINUTES = 5;

    private RegisteringTimeoutController() {
        throw new UnsupportedOperationException();
    }

    static void setTimeout(@NonNull final Context context, @NonNull final String providerName) {
        OPFLog.logMethod(context, providerName);
        final long when = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES);
        OPFLog.d(
                "Set registering timeout : %s",
                SimpleDateFormat.getDateTimeInstance(
                        DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US
                ).format(new Date(when))
        );

        final AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        final Intent intent = new Intent(context, RetryBroadcastReceiver.class);
        intent.setAction(ACTION_CHECK_REGISTERING_TIMEOUT);
        intent.putExtra(EXTRA_PROVIDER_NAME, providerName);

        alarmManager.set(
                AlarmManager.RTC,
                when,
                PendingIntent.getBroadcast(context, providerName.hashCode(), intent, 0)
        );
    }
}
