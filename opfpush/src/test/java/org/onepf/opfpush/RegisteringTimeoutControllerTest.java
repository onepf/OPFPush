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

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author antonpp
 * @since 16.03.2015
 */
@Config(emulateSdk = JELLY_BEAN_MR2, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class RegisteringTimeoutControllerTest {

    private static final String TEST_PROVIDER = "TEST_PROVIDER";
    private static final long TIMEOUT_MINUTES = 5;
    private static final long TIME_DELTA_MILLISECONDS = 100;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testSetTimeout() {
        final Context ctx = RuntimeEnvironment.application.getApplicationContext();
        final AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
        final ShadowAlarmManager shadowAlarmManager = shadowOf(alarmManager);

        final long whenExpected = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES);
        RegisteringTimeoutController.setTimeout(ctx, TEST_PROVIDER);

        final List<ShadowAlarmManager.ScheduledAlarm> alarms = shadowAlarmManager.getScheduledAlarms();
        for (ShadowAlarmManager.ScheduledAlarm alarm : alarms) {
            final PendingIntent operation = alarm.operation;
            final int creatorUid = operation.getCreatorUid();
            if (creatorUid == ctx.getApplicationInfo().uid) {
                Assert.assertTrue(Math.abs(whenExpected - alarm.triggerAtTime) < TIME_DELTA_MILLISECONDS);
                return;
            }
        }
        Assert.fail();
    }
}
