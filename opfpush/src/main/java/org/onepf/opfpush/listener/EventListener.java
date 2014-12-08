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

package org.onepf.opfpush.listener;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.model.State;

/**
 * Interface definition for a callback to be invoked when event
 * in {@link OPFPushHelper} is occurred.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 07.09.14.
 */
public interface EventListener {

    /**
     * New message received.
     *
     * @param providerName Name of provider received event.
     * @param extras       Data associated with message.
     */
    void onMessage(@NonNull String providerName, @Nullable Bundle extras);

    /**
     * Notification about deleted messages. Not all provider send this data or not send
     * count of deleted messages (in this case this value will be negative).
     *
     * @param providerName  Name of provider received event.
     * @param messagesCount Count of messages. Negative value if no info about count.
     */
    void onDeletedMessages(@NonNull String providerName, int messagesCount);

    /**
     * Provider registered successfully.
     *
     * @param providerName   Name of registered provider.
     * @param registrationId Registration id for push notification.
     */
    void onRegistered(@NonNull String providerName, @NonNull String registrationId);

    /**
     * Provider unregistered successfully.
     *
     * @param providerName   Name of unregistered provider.
     * @param registrationId Old registration id for push notification.
     */
    void onUnregistered(@NonNull String providerName, @NonNull String registrationId);

    /**
     * Provider registration failed. Provider can continue try to register
     * with exponential backoff (is {@code error} is recoverable) or can try to register next.
     *
     * @param providerName Name of provider in what error occur.
     * @param error        Occurred error
     */
    void onRegistrationError(@NonNull String providerName, @NonNull OPFError error);

    /**
     * Provider unregistration failed. Provider can continue try to unregister
     * with exponential backoff (is {@code error} is recoverable).
     *
     * @param providerName Name of provider in what error occur.
     * @param error        Occurred error
     */
    void onUnregistrationError(@NonNull String providerName, @NonNull OPFError error);

    /**
     * There is wrong state while registration was performed.
     *
     * @param providerName Name of provider in what error occur.
     * @param state        Current state.
     */
    void onRegistrationStateError(@NonNull String providerName, @NonNull State state);

    /**
     * There is wrong state while unregistration was performed.
     *
     * @param providerName Name of provider in what error occur.
     * @param state        Current state.
     */
    void onUnregistrationStateError(@NonNull String providerName, @NonNull State state);

    /**
     * {@code OpenPushHelper} can't find any available provider for register push.
     */
    void onNoAvailableProvider();

    /**
     * Provider registration or unregistration failed, but current state is {@code REGISTERED} or
     * {@code UNREGISTERED}.
     *
     * @param providerName Name of provider in what error occur.
     * @param error        Occurred error
     * @param state        Current state.
     */
    void onWrongStateError(@NonNull String providerName,
                           @NonNull OPFError error,
                           @NonNull State state);

    /**
     * Provider went to unavailable state. Is most case reason ot this is removing
     * host application of the provider.
     *
     * @param providerName Name of provider.
     */
    void onProviderBecameUnavailable(@NonNull String providerName);
}
