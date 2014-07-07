/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.openpush.gcm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.openpush.OpenPushLog;
import org.onepf.openpush.OpenPushProvider;
import org.onepf.openpush.OpenPushProviderHelper;
import org.onepf.openpush.PushListener;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    private static final String TAG = GcmIntentService.class.getSimpleName();

    public static final String ACTION_REGISTRATION = "onepf.org.onepf.openpush.gcm.REGISTRATION";
    public static final String ACTION_UNREGISTRATION = "onepf.org.onepf.openpush.gcm.UNREGISTRATION";
    public static final String ACTION_ERROR = "onepf.org.onepf.openpush.gcm.ERROR";
    public static final String ACTION_ERROR_SERVICE_NOT_AVAILABLE = "onepf.org.onepf.openpush.gcm.ERROR_SERVICE_NOT_AVAILABLE";
    public static final String ACTION_RETRY = "onepf.org.onepf.openpush.gcm.RETRY";

    public static final String EXTRA_MESSAGE = "message";

    private static final Random sRandom = new Random();

    private static final int MAX_BACKOFF_MS = (int) TimeUnit.SECONDS.toMillis(3600);

    private static final int DEFAULT_BACKOFF_MS = 3000;

    // token used to check intent origin
    private static final String TOKEN = Long.toBinaryString(sRandom.nextLong());

    private static final String EXTRA_TOKEN = "token";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    private PushListener mListener;

    @Override
    protected void onHandleIntent(Intent intent) {
        mListener = OpenPushProviderHelper.getInternalPushListener();

        if (OpenPushLog.isEnabled()) {
            Log.i(TAG, "Handling intent. " + OpenPushLog.intentToString(intent));
        }
        Bundle extras = intent.getExtras();
        if (ACTION_REGISTRATION.equals(intent.getAction())) {
            GcmProvider.resetBackoff(this, DEFAULT_BACKOFF_MS);
            onRegistered(intent.getStringExtra(EXTRA_MESSAGE));
        } else if (ACTION_UNREGISTRATION.equals(intent.getAction())) {
            GcmProvider.resetBackoff(this, DEFAULT_BACKOFF_MS);
            onUnregistered(intent.getStringExtra(EXTRA_MESSAGE));
        } else if (ACTION_ERROR.equals(intent.getAction())) {
            onError(intent.getStringExtra(EXTRA_MESSAGE));
        } else if (ACTION_ERROR_SERVICE_NOT_AVAILABLE.equals(intent.getAction())) {
            onServiceNotAvailable();
        } else if (ACTION_RETRY.equals(intent.getAction())) {
            OpenPushProvider instance = OpenPushProvider.getInstance();
            if (instance.isRegistered()) {
                instance.unregister();
            } else {
                instance.register();
            }
        } else {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            // The getMessageType() intent parameter must be the intent you received
            // in your BroadcastReceiver.
            String messageType = gcm.getMessageType(intent);

            if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
                /*
                 * Filter messages based on message type. Since it is likely that GCM will be
                 * extended in the future with new message types, just ignore any message types you're
                 * not interested in, or that you don't recognize.
                 */
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    onMessage(extras);
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void onServiceNotAvailable() {
        boolean retry = onRecoverableError();
        if (retry) {
            int backoffTimeMs = GcmProvider.getBackoff(this, DEFAULT_BACKOFF_MS);
            int nextAttempt = backoffTimeMs / 2 + sRandom.nextInt(backoffTimeMs);
            if (OpenPushLog.isEnabled()) {
                Log.d(TAG, "Scheduling registration retry, backoff = " + nextAttempt + " (" + backoffTimeMs + ")");
            }
            Intent retryIntent = new Intent(this, GcmIntentService.class);
            retryIntent.setAction(ACTION_RETRY);
            retryIntent.putExtra(EXTRA_TOKEN, TOKEN);
            scheduleIntent(retryIntent, nextAttempt);
            // Next retry should wait longer.
            if (backoffTimeMs < MAX_BACKOFF_MS) {
                GcmProvider.setBackoff(this, backoffTimeMs * 2);
            }
        } else {
            if (OpenPushLog.isEnabled()) {
                Log.d(TAG, "Not retrying failed operation");
            }
        }
    }

    private void scheduleIntent(Intent retryIntent, int time) {
        PendingIntent retryPendingIntent = PendingIntent.getService(this, 0, retryIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, retryPendingIntent);
    }

    //todo move to an interface/listener
    private boolean onRecoverableError() {
        return true;
    }

    private void onMessage(Bundle extras) {
        mListener.onMessage(this, extras, GcmProvider.NAME);
    }

    private void onError(String errorMessage) {
        mListener.onError(this, errorMessage, GcmProvider.NAME);
    }

    private void onRegistered(String registrationToken) {
        mListener.onRegistered(this, registrationToken, GcmProvider.NAME);
    }

    private void onUnregistered(String oldRegistrationToken) {
        mListener.onUnregistered(this, oldRegistrationToken, GcmProvider.NAME);
    }

}
