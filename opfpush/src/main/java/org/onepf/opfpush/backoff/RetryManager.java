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

package org.onepf.opfpush.backoff;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.RetryBroadcastReceiver;
import org.onepf.opfpush.model.Operation;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static org.onepf.opfpush.OPFConstants.ACTION_RETRY_REGISTER;
import static org.onepf.opfpush.OPFConstants.ACTION_RETRY_UNREGISTER;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;
import static org.onepf.opfpush.model.Operation.REGISTER;
import static org.onepf.opfpush.model.Operation.UNREGISTER;

/**
 * @author Roman Savin
 * @since 06.02.2015
 */
public final class RetryManager implements BackoffManager {

    private static RetryManager instance = null;

    @NonNull
    private Context appContext;

    @NonNull
    private BackoffManager backoffManager;

    @NonNull
    private AlarmManager alarmManager;

    private RetryManager(@NonNull final Context context,
                         @NonNull final BackoffManager backoffManager) {
        this.appContext = context.getApplicationContext();
        this.backoffManager = backoffManager;
        this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    @NonNull
    public static RetryManager getInstance(@NonNull final Context context,
                                           @NonNull final BackoffManager backoffManager) {
        if (instance == null) {
            instance = new RetryManager(context, backoffManager);
        }
        return instance;
    }

    @Override
    public boolean hasTries(@NonNull final String providerName,
                            @NonNull final Operation operation) {
        return backoffManager.hasTries(providerName, operation);
    }

    @Override
    public long getTryDelay(@NonNull final String providerName,
                            @NonNull final Operation operation) {
        return backoffManager.getTryDelay(providerName, operation);
    }

    @Override
    public void reset(@NonNull final String providerName, @NonNull final Operation operation) {
        backoffManager.reset(providerName, operation);
    }

    public void postRetryRegister(@NonNull final String providerName) {
        OPFPushLog.methodD(RetryManager.class, "postRetryRegister", providerName);
        postRetry(providerName, REGISTER, ACTION_RETRY_REGISTER);
    }

    public void postRetryUnregister(@NonNull final String providerName) {
        OPFPushLog.methodD(RetryManager.class, "postRetryUnregister", providerName);
        postRetry(providerName, UNREGISTER, ACTION_RETRY_UNREGISTER);
    }

    public void cancelRetryAllOperations(@NonNull final String providerName) {
        cancelRetryRegister(providerName);
        cancelRetryUnregister(providerName);
    }

    public void cancelRetryRegister(@NonNull final String providerName) {
        OPFPushLog.methodD(RetryManager.class, "cancelRetryRegister", providerName);
        cancelRetry(providerName, REGISTER, ACTION_RETRY_REGISTER);
    }

    public void cancelRetryUnregister(@NonNull final String providerName) {
        OPFPushLog.methodD(RetryManager.class, "cancelRetryUnregister", providerName);
        cancelRetry(providerName, UNREGISTER, ACTION_RETRY_UNREGISTER);
    }

    private void postRetry(@NonNull final String providerName,
                           @NonNull final Operation operation,
                           @NonNull final String action) {
        final long when = System.currentTimeMillis() + getTryDelay(providerName, operation);
        OPFPushLog.d("Post retry provider '%s' at %s", providerName,
                SimpleDateFormat.getDateTimeInstance().format(new Date(when)));

        final Intent intent = new Intent(appContext, RetryBroadcastReceiver.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_PROVIDER_NAME, providerName);

        alarmManager.set(
                AlarmManager.RTC,
                when,
                PendingIntent.getBroadcast(appContext, providerName.hashCode(), intent, 0)
        );
    }

    private void cancelRetry(@NonNull final String providerName,
                             @NonNull final Operation operation,
                             @NonNull final String action) {
        reset(providerName, operation);

        final Intent intent = new Intent(appContext, RetryBroadcastReceiver.class);
        intent.setAction(action);
        alarmManager.cancel(
                PendingIntent.getBroadcast(appContext, providerName.hashCode(), intent, 0)
        );
    }
}
