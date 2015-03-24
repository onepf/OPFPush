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

import org.onepf.opfpush.OPFConstants;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.model.RecoverablePushError;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.gcm.GCMConstants.PROVIDER_NAME;
import static org.onepf.opfpush.model.RecoverablePushError.Type.SERVICE_NOT_AVAILABLE;
import static org.onepf.opfpush.model.UnrecoverablePushError.Type.AUTHENTICATION_FAILED;
import static org.onepf.opfpush.model.UnrecoverablePushError.Type.UNKNOWN_ERROR;

/**
 * This {@link IntentService} does the actual handling of the GCM message.
 * {@link org.onepf.opfpush.gcm.GCMReceiver} (a {@code WakefulBroadcastReceiver}) holds a
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
    protected void onHandleIntent(final Intent intent) {
        OPFLog.logMethod(OPFUtils.toString(intent));

        @GCMAction String action = intent.getAction();
        if (GCMConstants.ACTION_REGISTRATION_CALLBACK.equals(action)) {
            if (intent.hasExtra(GCMConstants.EXTRA_ERROR_ID)) {
                final String errorId = intent.getStringExtra(GCMConstants.EXTRA_ERROR_ID);
                onRegistrationError(errorId);
            } else {
                onRegistered(intent.getStringExtra(GCMConstants.EXTRA_REGISTRATION_ID));
            }
        } else if (GCMConstants.ACTION_UNREGISTRATION_CALLBACK.equals(action)) {
            if (intent.hasExtra(GCMConstants.EXTRA_ERROR_ID)) {
                final String errorId = intent.getStringExtra(GCMConstants.EXTRA_ERROR_ID);
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
        OPFLog.logMethod(OPFUtils.toString(intent));
        OPFPush.getHelper().getReceivedMessageHandler().onMessage(PROVIDER_NAME, intent.getExtras());
    }

    private void onDeletedMessages() {
        OPFLog.logMethod();
        OPFPush.getHelper().getReceivedMessageHandler()
                .onDeletedMessages(PROVIDER_NAME, OPFConstants.MESSAGES_COUNT_UNKNOWN);
    }

    private void onRegistered(@NonNull final String registrationId) {
        OPFLog.logMethod(registrationId);
        OPFPush.getHelper().getReceivedMessageHandler().onRegistered(PROVIDER_NAME, registrationId);
    }

    private void onUnregistered(@Nullable final String oldRegistrationId) {
        OPFLog.logMethod(oldRegistrationId);
        OPFPush.getHelper().getReceivedMessageHandler().onUnregistered(PROVIDER_NAME, oldRegistrationId);
    }

    private void onRegistrationError(@NonNull final String errorId) {
        OPFLog.logMethod(errorId);
        final PushError error = convertError(errorId);
        OPFLog.d("Converted error : " + error);

        OPFPush.getHelper().getReceivedMessageHandler().onRegistrationError(PROVIDER_NAME, error);
    }

    private void onUnregistrationError(@NonNull final String errorId) {
        OPFLog.logMethod(errorId);
        final PushError error = convertError(errorId);
        OPFLog.d("Converted error : " + error);

        OPFPush.getHelper().getReceivedMessageHandler()
                .onUnregistrationError(PROVIDER_NAME, error);
    }

    private PushError convertError(@NonNull final String errorId) {
        switch (errorId) {
            case GCMConstants.ERROR_SERVICE_NOT_AVAILABLE:
                return new RecoverablePushError(SERVICE_NOT_AVAILABLE, PROVIDER_NAME, errorId);
            case GCMConstants.ERROR_AUTHENTICATION_FAILED:
                return new UnrecoverablePushError(AUTHENTICATION_FAILED, PROVIDER_NAME, errorId);
            default:
                OPFLog.e("Unknown GCM error : " + errorId);
                return new UnrecoverablePushError(UNKNOWN_ERROR, PROVIDER_NAME, errorId);
        }
    }
}
