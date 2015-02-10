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
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.io.Serializable;
import java.util.Map;

import static org.onepf.opfpush.OPFConstants.ACTION_NO_AVAILABLE_PROVIDER;
import static org.onepf.opfpush.OPFConstants.ACTION_RECEIVE;
import static org.onepf.opfpush.OPFConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.OPFConstants.ACTION_UNREGISTRATION;
import static org.onepf.opfpush.OPFConstants.EXTRA_MESSAGE_COUNT;
import static org.onepf.opfpush.OPFConstants.EXTRA_MESSAGE_TYPE;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;
import static org.onepf.opfpush.OPFConstants.EXTRA_REGISTRATION_ERRORS;
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
            @Nullable final EventListener eventListener
    ) {
        OPFLog.methodD(eventListener);

        if (eventListener != null) {
            return createMainLooperWrapper(eventListener);
        } else {
            return createBroadcastSender();
        }
    }

    @NonNull
    private static EventListener createMainLooperWrapper(@NonNull final EventListener eventListener) {
        OPFLog.methodD(eventListener);
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
                        OPFLog.d("Post onMessage(%1$s, %2$s)", providerName, extras);
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
                        OPFLog.d("Post onDeletedMessages(%1$s, %2$s)", providerName, messagesCount);
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
                        OPFLog.d("Post onRegistered(%1$s, %2$s)", providerName, registrationId);
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
                        OPFLog.d("Post onUnregistered(%1$s, %2$s)", providerName, registrationId);
                        eventListener.onUnregistered(context, providerName, registrationId);
                    }
                });
            }

            @Override
            public void onNoAvailableProvider(
                    @NonNull final Context context,
                    @NonNull final Map<String, OPFError> registrationErrors) {
                OPFUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        OPFLog.d("Post onNoAvailableProvider()");
                        eventListener.onNoAvailableProvider(context, registrationErrors);
                    }
                });
            }
        };
    }

    @NonNull
    private static EventListener createBroadcastSender() {
        OPFLog.methodD("");
        return new EventListener() {
            @Override
            public void onMessage(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @Nullable final Bundle extras
            ) {
                OPFLog.d("SendBroadcast onMessage(%1$s, %2$s)", providerName, extras);

                final Intent intent = new Intent(ACTION_RECEIVE);
                if (extras != null) {
                    intent.putExtras(extras);
                }
                intent.putExtra(EXTRA_MESSAGE_TYPE, MESSAGE_TYPE_MESSAGE);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);

                context.sendBroadcast(intent);
            }

            @Override
            public void onDeletedMessages(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    final int messagesCount
            ) {
                OPFLog.d("SendBroadcast onDeletedMessages(%1$s, %2$s)", providerName, messagesCount);
                final Intent intent = new Intent(ACTION_RECEIVE);
                intent.putExtra(EXTRA_MESSAGE_TYPE, MESSAGE_TYPE_DELETED);
                intent.putExtra(EXTRA_MESSAGE_COUNT, messagesCount);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                context.sendBroadcast(intent);
            }

            @Override
            public void onRegistered(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @NonNull final String registrationId
            ) {
                OPFLog.d("SendBroadcast onRegistered(%1$s, %2$s)", providerName, registrationId);
                final Intent intent = new Intent(ACTION_REGISTRATION);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                intent.putExtra(EXTRA_REGISTRATION_ID, registrationId);
                context.sendBroadcast(intent);
            }

            @Override
            public void onUnregistered(
                    @NonNull final Context context,
                    @NonNull final String providerName,
                    @Nullable final String registrationId
            ) {
                OPFLog.d("SendBroadcast onUnregistered(%1$s, %2$s)", providerName, registrationId);
                final Intent intent = new Intent(ACTION_UNREGISTRATION);
                intent.putExtra(EXTRA_PROVIDER_NAME, providerName);
                intent.putExtra(EXTRA_REGISTRATION_ID, registrationId);
                context.sendBroadcast(intent);
            }

            @Override
            public void onNoAvailableProvider(
                    @NonNull final Context context,
                    @NonNull final Map<String, OPFError> registrationErrors
            ) {
                OPFLog.d("SendBroadcast onNoAvailableProvider()");
                final Intent intent = new Intent(ACTION_NO_AVAILABLE_PROVIDER);
                final Bundle extras = new Bundle();
                extras.putSerializable(EXTRA_REGISTRATION_ERRORS, (Serializable) registrationErrors);
                intent.putExtras(extras);
                context.sendBroadcast(intent);
            }
        };
    }
}
