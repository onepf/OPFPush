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

import org.onepf.opfpush.model.MessageType;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.util.Utils;

import static org.onepf.opfpush.OPFConstants.ACTION_NO_AVAILABLE_PROVIDER;
import static org.onepf.opfpush.OPFConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.OPFConstants.ACTION_UNREGISTRATION;
import static org.onepf.opfpush.OPFConstants.EXTRA_ERROR;
import static org.onepf.opfpush.OPFConstants.EXTRA_MESSAGE_TYPE;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;
import static org.onepf.opfpush.OPFConstants.EXTRA_REGISTRATION_ID;
import static org.onepf.opfpush.OPFConstants.MESSAGES_COUNT_UNKNOWN;

/**
 * @author Roman Savin
 * @since 25.12.14
 */
public abstract class OPFPushReceiver extends BroadcastReceiver {

    protected abstract void onMessage(@NonNull final Context context,
                                      @NonNull final String providerName,
                                      @NonNull final Bundle extras);

    protected abstract void onDeletedMessage(@NonNull final Context context,
                                             @NonNull final String providerName,
                                             final int messagesCount);

    protected abstract void onRegistered(@NonNull final Context context,
                                         @NonNull final String providerName,
                                         @NonNull final String registrationId);

    protected abstract void onUnregistered(@NonNull final Context context,
                                           @NonNull final String providerName,
                                           @NonNull final String oldRegistrationId);

    protected abstract void onRegistrationError(@NonNull final Context context,
                                                @NonNull final String providerName,
                                                @NonNull final OPFError error);

    protected abstract void onUnregistrationError(@NonNull final Context context,
                                                  @NonNull final String providerName,
                                                  @NonNull final OPFError error);

    protected abstract void onNoAvailableProvider(@NonNull final Context context);

    @Override
    public final void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "onReceive", context, Utils.toString(intent));

        @OPFAction final String action = intent.getAction();
        switch (action) {
            case ACTION_NO_AVAILABLE_PROVIDER:
                onNoAvailableProvider(context);
                break;
            case ACTION_REGISTRATION:
                handleRegistrationAction(context, intent);
                break;
            case ACTION_UNREGISTRATION:
                handleUnregistrationAction(context, intent);
                break;
            default:
                handleReceiveAction(context, intent);
                break;
        }
    }

    private void handleRegistrationAction(@NonNull final Context context,
                                          @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "handleRegistrationAction", context, Utils.toString(intent));

        final String providerName = intent.getStringExtra(EXTRA_PROVIDER_NAME);
        if (intent.hasExtra(EXTRA_ERROR)) {
            onRegistrationError(
                    context,
                    providerName,
                    (OPFError) intent.getSerializableExtra(EXTRA_ERROR)
            );
        } else {
            onRegistered(
                    context,
                    providerName,
                    intent.getStringExtra(EXTRA_REGISTRATION_ID)
            );
        }
    }

    private void handleUnregistrationAction(@NonNull final Context context,
                                            @NonNull final Intent intent) {
        OPFPushLog.methodD(OPFPushReceiver.class, "handleUnregistrationAction", context, Utils.toString(intent));

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
        OPFPushLog.methodD(OPFPushReceiver.class, "handleReceiveAction", context, Utils.toString(intent));

        final String providerName = intent.getStringExtra(EXTRA_PROVIDER_NAME);
        final MessageType messageType = (MessageType) intent.getSerializableExtra(EXTRA_MESSAGE_TYPE);
        switch (messageType) {
            case MESSAGE_TYPE_DELETED:
                onDeletedMessage(
                        context,
                        providerName,
                        intent.getIntExtra(EXTRA_MESSAGE_TYPE, MESSAGES_COUNT_UNKNOWN)
                );
                break;
            case MESSAGE_TYPE_MESSAGE:
                onMessage(context, providerName, intent.getExtras());
                break;
        }
    }
}
