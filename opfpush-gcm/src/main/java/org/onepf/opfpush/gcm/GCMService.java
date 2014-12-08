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

package org.onepf.opfpush.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.OPFPushHelper;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 *
 * @author Roman Savin
 */
public class GCMService extends IntentService {

    public GCMService() {
        super("GCMService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        @GCMAction String action = intent.getAction();
        if (GCMConstants.ACTION_REGISTRATION_CALLBACK.equals(action)) {
            if (intent.hasExtra(GCMConstants.EXTRA_ERROR_ID)) {
                @GCMError String errorId
                        = intent.getStringExtra(GCMConstants.EXTRA_ERROR_ID);
                onRegistrationError(errorId);
            } else {
                onRegistered(intent.getStringExtra(GCMConstants.EXTRA_REGISTRATION_ID));
            }
        } else if (GCMConstants.ACTION_UNREGISTRATION_CALLBACK.equals(action)) {
            if (intent.hasExtra(GCMConstants.EXTRA_ERROR_ID)) {
                @GCMError String errorId
                        = intent.getStringExtra(GCMConstants.EXTRA_ERROR_ID);
                onUnregistrationError(errorId);
            } else {
                onUnregistered(intent.getStringExtra(GCMConstants.EXTRA_REGISTRATION_ID));
            }
        } else if (intent.getExtras() != null) {
            final String messageType = GoogleCloudMessaging.getInstance(this).getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                onDeletedMessages();
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                onMessage(intent);
            }
        }
        GCMReceiver.completeWakefulIntent(intent);
    }

    private void onMessage(@NonNull final Intent intent) {
        OPFPushHelper.getInstance(GCMService.this).getReceivedMessageHandler()
                .onMessage(GCMProvider.NAME, intent.getExtras());
    }

    private void onDeletedMessages() {
        OPFPushHelper.getInstance(GCMService.this).getReceivedMessageHandler()
                .onDeletedMessages(GCMProvider.NAME, OPFPushHelper.MESSAGES_COUNT_UNKNOWN);
    }

    private void onRegistered(@NonNull final String registrationId) {
        OPFPushHelper.getInstance(this).getReceivedMessageHandler()
                .onRegistered(GCMProvider.NAME, registrationId);
    }

    private void onUnregistered(@Nullable final String oldRegistrationToken) {
        OPFPushHelper.getInstance(this).getReceivedMessageHandler()
                .onUnregistered(GCMProvider.NAME, oldRegistrationToken);
    }

    private void onRegistrationError(@NonNull @GCMError final String errorId) {
        OPFPushHelper.getInstance(this).getReceivedMessageHandler()
                .onRegistrationError(GCMProvider.NAME, convertError(errorId));
    }

    private void onUnregistrationError(@NonNull @GCMError final String errorId) {
        OPFPushHelper.getInstance(this).getReceivedMessageHandler()
                .onUnregistrationError(GCMProvider.NAME, convertError(errorId));
    }

    private OPFError convertError(@NonNull @GCMError final String errorId) {
        final OPFError error;
        if (GCMConstants.ERROR_SERVICE_NOT_AVAILABLE.equals(errorId)) {
            error = OPFError.SERVICE_NOT_AVAILABLE;
        } else if (GCMConstants.ERROR_AUTHENTICATION_FAILED.equals(errorId)) {
            error = OPFError.AUTHENTICATION_FAILED;
        } else {
            throw new OPFPushException(String.format("Unknown error '%s'.", errorId));
        }

        return error;
    }
}
