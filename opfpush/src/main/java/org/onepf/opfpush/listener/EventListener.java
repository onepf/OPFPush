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
 * Interface definition for a callback to be invoked when an event
 * in {@link OPFPushHelper} is occurred.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 07.09.14.
 */
public interface EventListener {

    /**
     * Called when a new message has been received.
     *
     * @param context      application context.
     * @param providerName Name of provider which has received event.
     * @param extras       Data associated with message.
     */
    void onMessage(@NonNull Context context, @NonNull String providerName, @Nullable Bundle extras);

    /**
     * Called when notification about deleted messages has been received.
     * Some providers doesn't send the count of deleted messages. In this case message count will be equals
     * {@link org.onepf.opfpush.OPFConstants#MESSAGES_COUNT_UNKNOWN}
     *
     * @param context       application context.
     * @param providerName  Name of provider which has received event.
     * @param messagesCount Count of messages. {@link org.onepf.opfpush.OPFConstants#MESSAGES_COUNT_UNKNOWN} value
     *                      if there isn't information about count.
     */
    void onDeletedMessages(@NonNull Context context, @NonNull String providerName, int messagesCount);

    /**
     * Called when message about successful registration has been received.
     *
     * @param context        application context.
     * @param providerName   Name of provider which has received event.
     * @param registrationId Registration ID for push notifications.
     */
    void onRegistered(@NonNull Context context, @NonNull String providerName, @NonNull String registrationId);

    /**
     * Called when {@link org.onepf.opfpush.OPFPushHelper} become to {@code UNREGISTERED} state.
     * Calling of this method doesn't mean that provider is already unregistered. Unregistration
     * is performed in the background and is retried if unregistration error is occurred.
     *
     * @param context        application context.
     * @param providerName   Name of provider which has received event.
     * @param registrationId Old registration ID for push notification.
     */
    void onUnregistered(@NonNull Context context, @NonNull String providerName, @Nullable String registrationId);

    /**
     * {@code OpenPushHelper} can't find any available provider for register push.
     * You should notify user that push notifications will not be received.
     * <p/>
     * A push provider can be unavailable in two common reasons:
     * 1) {@link org.onepf.opfpush.PushProvider#isAvailable()} method has returned false value.
     * 2) Some unrecoverable registration error has been occurred.
     * In the second case you can get {@link org.onepf.opfpush.model.OPFError} object from {@code registrationErrors}
     * map. You can notify user about occurred error, if the error can be resolved by user.
     * For example if you get {@link org.onepf.opfpush.model.OPFError#AUTHENTICATION_FAILED} for GCM push provider,
     * you can ask user add the google account.
     *
     * @param context            application context.
     * @param registrationErrors map that stores registration errors by names of push providers.
     *                           If there isn't any registration errors, the map is empty.
     */
    void onNoAvailableProvider(@NonNull Context context, @NonNull Map<String, OPFError> registrationErrors);
}
