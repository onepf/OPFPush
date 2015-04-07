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

package org.onepf.opfpush.backoff;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.onepf.opfpush.ConnectivityChangeReceiver;
import org.onepf.opfpush.RetryBroadcastReceiver;
import org.onepf.opfpush.model.Operation;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.exception.InitException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static android.content.Context.ALARM_SERVICE;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
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

    private static volatile RetryManager instance;

    @NonNull
    private final Context appContext;

    @NonNull
    private final BackoffManager backoffManager;

    @NonNull
    private final AlarmManager alarmManager;

    private final Set<Pair<String, String>> retryProvidersActions;

    @Nullable
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    private RetryManager(@NonNull final Context context,
                         @NonNull final BackoffManager backoffManager) {
        this.appContext = context.getApplicationContext();
        this.backoffManager = backoffManager;
        this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        this.retryProvidersActions = new HashSet<>();
    }

    @NonNull
    @SuppressWarnings("PMD.NonThreadSafeSingleton")
    public static RetryManager init(@NonNull final Context context,
                            @NonNull final BackoffManager backoffManager) {
        OPFChecks.checkThread(true);
        checkInit(false);
        return instance = new RetryManager(context, backoffManager);
    }

    @NonNull
    public static RetryManager getInstance() {
        OPFChecks.checkThread(true);
        checkInit(true);
        return instance;
    }

    private static void checkInit(final boolean initExpected) {
        final boolean isInit = instance != null;
        if (initExpected != isInit) {
            throw new InitException(isInit);
        }
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
        OPFLog.logMethod(providerName);
        postRetry(providerName, REGISTER, ACTION_RETRY_REGISTER);
    }

    public void postRetryUnregister(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        postRetry(providerName, UNREGISTER, ACTION_RETRY_UNREGISTER);
    }

    public void cancelRetryAllOperations(@NonNull final String providerName) {
        cancelRetryRegister(providerName);
        cancelRetryUnregister(providerName);
    }

    public void cancelRetryRegister(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        cancelRetry(providerName, REGISTER, ACTION_RETRY_REGISTER);
    }

    public void cancelRetryUnregister(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        cancelRetry(providerName, UNREGISTER, ACTION_RETRY_UNREGISTER);
    }

    @NonNull
    public Set<Pair<String, String>> getRetryProvidersActions() {
        OPFLog.logMethod();
        return retryProvidersActions;
    }

    private void postRetry(@NonNull final String providerName,
                           @NonNull final Operation operation,
                           @NonNull final String action) {
        final long when = System.currentTimeMillis() + getTryDelay(providerName, operation);
        OPFLog.d("Post retry %s provider '%s' at %s",
                operation,
                providerName,
                SimpleDateFormat.getDateTimeInstance(
                        DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US
                ).format(new Date(when))
        );

        retryProvidersActions.add(new Pair<>(providerName, action));
        registerConnectivityChangeReceiver();

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

        retryProvidersActions.remove(new Pair<>(providerName, action));
        if (retryProvidersActions.isEmpty()) {
            unregisterConnectivityChangeReceiver();
        }

        final Intent intent = new Intent(appContext, RetryBroadcastReceiver.class);
        intent.setAction(action);

        final PendingIntent pendingIntent = PendingIntent
                .getBroadcast(appContext, providerName.hashCode(), intent, 0);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void registerConnectivityChangeReceiver() {
        OPFLog.logMethod();
        if (connectivityChangeReceiver == null) {
            connectivityChangeReceiver = new ConnectivityChangeReceiver();
            appContext.registerReceiver(connectivityChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));
        }
    }

    private void unregisterConnectivityChangeReceiver() {
        OPFLog.logMethod();
        if (connectivityChangeReceiver != null) {
            appContext.unregisterReceiver(connectivityChangeReceiver);
            connectivityChangeReceiver = null;
        }
    }
}
