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

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.onepf.openpush.OpenPushConstants;
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.RegistrationResult;

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
        if (GCMConstants.ACTION_REGISTRATION.equals(intent.getAction())) {
            onRegistered(intent.getStringExtra(GCMConstants.EXTRA_TOKEN));
        } else if (GCMConstants.ACTION_UNREGISTRATION.equals(intent.getAction())) {
            onUnregistered(intent.getStringExtra(GCMConstants.EXTRA_TOKEN));
        } else if (GCMConstants.ACTION_ERROR.equals(intent.getAction())) {
            onError(intent.getStringExtra(GCMConstants.EXTRA_ERROR_ID));
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
        OpenPushHelper.getInstance(this).onDeletedMessages(GCMProvider.NAME, 1);
    }

    private void onMessage(Intent intent) {
        OpenPushHelper.getInstance(this).onMessage(GCMProvider.NAME, intent.getExtras());
    }

    private void onError(@NotNull
                         @MagicConstant(stringValues = {
                                 GCMConstants.ERROR_AUTHEFICATION_FAILED,
                                 GCMConstants.ERROR_SERVICE_NOT_AVAILABLE
                         }) String errorId) {
        int error;
        if (errorId.equals(GCMConstants.ERROR_SERVICE_NOT_AVAILABLE)) {
            error = OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE;
        } else if (errorId.equals(GCMConstants.ERROR_AUTHEFICATION_FAILED)) {
            error = OpenPushConstants.ERROR_AUTHEFICATION_FAILED;
        } else {
            error = OpenPushConstants.ERROR_UNKNOWN;
        }

        OpenPushHelper helper = OpenPushHelper.getInstance(this);
        if (helper.getState() == OpenPushHelper.STATE_REGISTRATION_RUNNING) {
            helper.onRegistrationEnd(new RegistrationResult(GCMProvider.NAME, error));
        } else if (helper.getState() == OpenPushHelper.STATE_UNREGISTRATION_RUNNING) {
            helper.onUnregistrationEnd(new RegistrationResult(GCMProvider.NAME, error));
        }
    }

    private void onRegistered(String registrationToken) {
        OpenPushHelper.getInstance(this)
                .onRegistrationEnd(new RegistrationResult(GCMProvider.NAME, registrationToken));
    }

    private void onUnregistered(String oldRegistrationToken) {
        OpenPushHelper.getInstance(this)
                .onUnregistrationEnd(new RegistrationResult(GCMProvider.NAME, oldRegistrationToken));
    }

}