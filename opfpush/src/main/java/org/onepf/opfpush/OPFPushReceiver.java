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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.MessageType;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfutils.OPFUtils;

import java.util.Map;

import static org.onepf.opfpush.OPFConstants.ACTION_NO_AVAILABLE_PROVIDER;
import static org.onepf.opfpush.OPFConstants.ACTION_RECEIVE;
import static org.onepf.opfpush.OPFConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.OPFConstants.ACTION_UNREGISTRATION;
import static org.onepf.opfpush.OPFConstants.EXTRA_ERROR;
import static org.onepf.opfpush.OPFConstants.EXTRA_MESSAGE_COUNT;
import static org.onepf.opfpush.OPFConstants.EXTRA_MESSAGE_TYPE;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;
import static org.onepf.opfpush.OPFConstants.EXTRA_REGISTRATION_ERRORS;
import static org.onepf.opfpush.OPFConstants.EXTRA_REGISTRATION_ID;
import static org.onepf.opfpush.OPFConstants.MESSAGES_COUNT_UNKNOWN;

/**
 * @author Roman Savin
 * @since 25.12.14
 */
public abstract class OPFPushReceiver extends BroadcastReceiver implements EventListener {

    @Override
    public final void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "onReceive", context, OPFUtils.toString(intent));

        @OPFAction final String action = intent.getAction();
        switch (action) {
            case ACTION_NO_AVAILABLE_PROVIDER:
                handleNoAvailableProvider(context, intent);
                break;
            case ACTION_REGISTRATION:
                handleRegistrationAction(context, intent);
                break;
            case ACTION_UNREGISTRATION:
                handleUnregistrationAction(context, intent);
                break;
            case ACTION_RECEIVE:
                handleReceiveAction(context, intent);
                break;
        }
    }

    private void handleNoAvailableProvider(@NonNull final Context context,
                                           @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "handleNoAvailableProvider", context, OPFUtils.toString(intent));

        final Bundle extras = intent.getExtras();
        @SuppressWarnings("unchecked")
        final Map<String, OPFError> registrationErrors =
                (Map<String, OPFError>) extras.getSerializable(EXTRA_REGISTRATION_ERRORS);
        onNoAvailableProvider(context, registrationErrors);
    }

    private void handleRegistrationAction(@NonNull final Context context,
                                          @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "handleRegistrationAction", context, OPFUtils.toString(intent));

        final String providerName = intent.getStringExtra(EXTRA_PROVIDER_NAME);
        onRegistered(
                context,
                providerName,
                intent.getStringExtra(EXTRA_REGISTRATION_ID)
        );
    }

    private void handleUnregistrationAction(@NonNull final Context context,
                                            @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "handleUnregistrationAction", context, OPFUtils.toString(intent));

        final String providerName = intent.getStringExtra(EXTRA_PROVIDER_NAME);
        if (intent.hasExtra(EXTRA_ERROR)) {
            onUnregistrationError(
                    context,
                    providerName,
                    (OPFError) intent.getSerializableExtra(EXTRA_ERROR)
            );
        } else {
            onUnregistered(
                    context,
                    providerName,
                    intent.getStringExtra(EXTRA_REGISTRATION_ID)
            );
        }
    }

    private void handleReceiveAction(@NonNull final Context context,
                                     @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "handleReceiveAction", context, OPFUtils.toString(intent));

        final String providerName = intent.getStringExtra(EXTRA_PROVIDER_NAME);
        final MessageType messageType = (MessageType) intent.getSerializableExtra(EXTRA_MESSAGE_TYPE);
        switch (messageType) {
            case MESSAGE_TYPE_DELETED:
                onDeletedMessages(
                        context,
                        providerName,
                        intent.getIntExtra(EXTRA_MESSAGE_COUNT, MESSAGES_COUNT_UNKNOWN)
                );
                break;
            case MESSAGE_TYPE_MESSAGE:
                onMessage(context, providerName, intent.getExtras());
                break;
        }
    }
}
