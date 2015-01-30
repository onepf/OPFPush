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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.OPFConstants.ACTION_NO_AVAILABLE_PROVIDER;
import static org.onepf.opfpush.OPFConstants.ACTION_RECEIVE;
import static org.onepf.opfpush.OPFConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.OPFConstants.ACTION_UNREGISTRATION;
import static org.onepf.opfpush.OPFConstants.EXTRA_ERROR;
import static org.onepf.opfpush.OPFConstants.EXTRA_MESSAGE_COUNT;
import static org.onepf.opfpush.OPFConstants.EXTRA_MESSAGE_TYPE;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;
import static org.onepf.opfpush.OPFConstants.EXTRA_REGISTRATION_ID;
import static org.onepf.opfpush.model.MessageType.MESSAGE_TYPE_DELETED;
import static org.onepf.opfpush.model.MessageType.MESSAGE_TYPE_MESSAGE;

/**
 * @author Roman Savin
 * @since 26.12.14
 */
final class EventListenerWrapperCreator {

    private EventListenerWrapperCreator() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    static EventListener getEventListenerWrapper(
            @NonNull final Context context,
            @Nullable final EventListener eventListener
    ) {
        OPFPushLog.methodD(EventListenerWrapperCreator.class, "getEventListenerWrapper", context, eventListener);

        if (eventListener != null) {
            return createMainLooperWrapper(eventListener);
        } else {
            return createBroadcastSender(context.getApplicationContext());
        }
    }

    @NonNull
    private static EventListener createMainLooperWrapper(@NonNull final EventListener eventListener) {
        OPFPushLog.methodD(EventListenerWrapperCreator.class, "createMainLooperWrapper", eventListener);
        return new EventListener() {

            @Override
            public void onMessage(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @Nullable final Bundle extras
            ) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFPushLog.d("Post onMessage(%1$s, %2$s)", providerName, extras);
                        eventListener.onMessage(context, providerName, extras);
                    }
                });
            }

            @Override
            public void onDeletedMessages(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    final int messagesCount
            ) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFPushLog.d("Post onDeletedMessages(%1$s, %2$s)", providerName, messagesCount);
                        eventListener.onDeletedMessages(context, providerName, messagesCount);
                    }
                });
            }

            @Override
            public void onRegistered(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @NonNull final String registrationId
            ) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFPushLog.d("Post onRegistered(%1$s, %2$s)", providerName, registrationId);
                        eventListener.onRegistered(context, providerName, registrationId);
                    }
                });
            }

            @Override
            public void onUnregistered(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @Nullable final String registrationId
            ) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFPushLog.d("Post onUnregistered(%1$s, %2$s)", providerName, registrationId);
                        eventListener.onUnregistered(context, providerName, registrationId);
                    }
                });
            }

            @Override
            public void onRegistrationError(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @NonNull final OPFError error
            ) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFPushLog.d("Post onRegistrationError(%1$s, %2$s)", providerName, error);
                        eventListener.onRegistrationError(context, providerName, error);
                    }
                });
            }

            @Override
            public void onUnregistrationError(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @NonNull final OPFError error
            ) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFPushLog.d("Post onUnregistrationError(%1$s, %2$s)", providerName, error);
                        eventListener.onUnregistrationError(context, providerName, error);
                    }
                });
            }

            @Override
            public void onNoAvailableProvider(@NonNull final Context context) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFPushLog.d("Post onNoAvailableProvider()");
                        eventListener.onNoAvailableProvider(context);
                    }
                });
            }
        };
    }

    @NonNull
    private static EventListener createBroadcastSender(@NonNull final Context appContext) {
        OPFPushLog.methodD(EventListenerWrapperCreator.class, "createBroadcastSender", appContext);
        return new EventListener() {
            @Override
            public void onMessage(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @Nullable final Bundle extras
            ) {
                OPFPushLog.d("SendBroadcast onMessage(%1$s, %2$s)", providerName, extras);

                final Intent intent = new Intent(ACTION_RECEIVE);
                if (extras != null) {
                    intent.putExtras(extras);
                }
                intent.putExtra(EXTRA_MESSAGE_TYPE, MESSAGE_TYPE_MESSAGE);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);

                appContext.sendBroadcast(intent);
            }

            @Override
            public void onDeletedMessages(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    final int messagesCount
            ) {
                OPFPushLog.d("SendBroadcast onDeletedMessages(%1$s, %2$s)", providerName, messagesCount);
                final Intent intent = new Intent(ACTION_RECEIVE);
                intent.putExtra(EXTRA_MESSAGE_TYPE, MESSAGE_TYPE_DELETED);
                intent.putExtra(EXTRA_MESSAGE_COUNT, messagesCount);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                appContext.sendBroadcast(intent);
            }

            @Override
            public void onRegistered(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @NonNull final String registrationId
            ) {
                OPFPushLog.d("SendBroadcast onRegistered(%1$s, %2$s)", providerName, registrationId);
                final Intent intent = new Intent(ACTION_REGISTRATION);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                intent.putExtra(EXTRA_REGISTRATION_ID, registrationId);
                appContext.sendBroadcast(intent);
            }

            @Override
            public void onUnregistered(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @Nullable final String registrationId
            ) {
                OPFPushLog.d("SendBroadcast onUnregistered(%1$s, %2$s)", providerName, registrationId);
                final Intent intent = new Intent(ACTION_UNREGISTRATION);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                intent.putExtra(EXTRA_REGISTRATION_ID, registrationId);
                appContext.sendBroadcast(intent);
            }

            @Override
            public void onRegistrationError(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @NonNull final OPFError error
            ) {
                OPFPushLog.d("SendBroadcast onRegistrationError(%1$s, %2$s)", providerName, error);
                final Intent intent = new Intent(ACTION_REGISTRATION);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                intent.putExtra(EXTRA_ERROR, error);
                appContext.sendBroadcast(intent);
            }

            @Override
            public void onUnregistrationError(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @NonNull final OPFError error
            ) {
                OPFPushLog.d("SendBroadcast onUnregistrationError(%1$s, %2$s)", providerName, error);
                final Intent intent = new Intent(ACTION_UNREGISTRATION);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                intent.putExtra(EXTRA_ERROR, error);
                appContext.sendBroadcast(intent);
            }

            @Override
            public void onNoAvailableProvider(@NonNull final Context context) {
                OPFPushLog.d("SendBroadcast onNoAvailableProvider()");
                final Intent intent = new Intent(ACTION_NO_AVAILABLE_PROVIDER);
                appContext.sendBroadcast(intent);
            }
        };
    }
}
