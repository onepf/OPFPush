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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.model.OPFError;

import java.util.Map;

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
     * @param context      application context.
     * @param providerName Name of provider received event.
     * @param extras       Data associated with message.
     */
    void onMessage(@NonNull Context context, @NonNull String providerName, @Nullable Bundle extras);

    /**
     * Notification about deleted messages. Not all provider send this data or not send
     * count of deleted messages (in this case this value will be negative).
     *
     * @param context       application context.
     * @param providerName  Name of provider received event.
     * @param messagesCount Count of messages. Negative value if no info about count.
     */
    void onDeletedMessages(@NonNull Context context, @NonNull String providerName, int messagesCount);

    /**
     * Provider registered successfully.
     *
     * @param context        application context.
     * @param providerName   Name of registered provider.
     * @param registrationId Registration id for push notification.
     */
    void onRegistered(@NonNull Context context, @NonNull String providerName, @NonNull String registrationId);

    /**
     * Provider unregistered successfully.
     *
     * @param context        application context.
     * @param providerName   Name of unregistered provider.
     * @param registrationId Old registration id for push notification.
     */
    void onUnregistered(@NonNull Context context, @NonNull String providerName, @Nullable String registrationId);

    /**
     * {@code OpenPushHelper} can't find any available provider for register push.
     * Notify user that push notifications will not be received.
     * Try to add more push providers to opfpush configurations.
     *
     * @param context application context.
     */
    void onNoAvailableProvider(@NonNull Context context, @NonNull Map<String, OPFError> registerationErrors);
}
