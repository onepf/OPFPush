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

package org.onepf.openpush.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.openpush.*;
import org.onepf.openpush.Error;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GCMService extends IntentService {

    public GCMService() {
        super("GCMService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        @GCMConstants.GCMAction String action = intent.getAction();
        if (GCMConstants.ACTION_REGISTRATION.equals(action)) {
            if (intent.hasExtra(GCMConstants.EXTRA_ERROR_ID)) {
                @GCMConstants.GCMError String errorId
                        = intent.getStringExtra(GCMConstants.EXTRA_ERROR_ID);
                onError(errorId, action);
            } else {
                onRegistered(intent.getStringExtra(GCMConstants.EXTRA_TOKEN));
            }
        } else if (GCMConstants.ACTION_UNREGISTRATION.equals(action)) {
            if (intent.hasExtra(GCMConstants.EXTRA_ERROR_ID)) {
                @GCMConstants.GCMError String errorId
                        = intent.getStringExtra(GCMConstants.EXTRA_ERROR_ID);
                onError(errorId, action);
            } else {
                onUnregistered(intent.getStringExtra(GCMConstants.EXTRA_TOKEN));
            }
        } else if (intent.getExtras() != null) {
            String messageType = GoogleCloudMessaging.getInstance(this).getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                onDeletedMessages();
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                onMessage(intent);
            }
        }
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    protected void onDeletedMessages() {
        OpenPushHelper.getInstance(GCMService.this)
                .onDeletedMessages(GCMProvider.NAME, 1);
    }

    private void onMessage(final Intent intent) {
        OpenPushHelper.getInstance(GCMService.this)
                .onMessage(GCMProvider.NAME, intent.getExtras());
    }

    private void onError(@GCMConstants.GCMError String errorId,
                         @GCMConstants.GCMAction String action) {
        final Error error;
        if (GCMConstants.ERROR_SERVICE_NOT_AVAILABLE.equals(errorId)) {
            error = Error.SERVICE_NOT_AVAILABLE;
        } else if (GCMConstants.ERROR_AUTHENTICATION_FAILED.equals(errorId)) {
            error = Error.AUTHENTICATION_FAILED;
        } else {
            throw new OpenPushException(String.format("Unknown error '%s'.", errorId));
        }

        final boolean recoverableError = GCMConstants.ERROR_SERVICE_NOT_AVAILABLE.equals(errorId);
        if (GCMConstants.ACTION_REGISTRATION.equals(action)
                || GCMConstants.ACTION_UNREGISTRATION.equals(action))
            OpenPushHelper.getInstance(GCMService.this)
                    .onResult(Result.error(GCMProvider.NAME, error, recoverableError));
        else {
            throw new OpenPushException(String.format("Unknown action '%s'.", action));
        }
    }

    private void onRegistered(final String registrationToken) {
        OpenPushHelper.getInstance(GCMService.this)
                .onResult(Result.success(GCMProvider.NAME, registrationToken));
    }

    private void onUnregistered(final String oldRegistrationToken) {
        OpenPushHelper.getInstance(GCMService.this)
                .onResult(Result.success(GCMProvider.NAME, oldRegistrationToken));
    }

}