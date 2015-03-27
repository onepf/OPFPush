/*
 * Copyright 2012-2015 One Platform Foundation
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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.model.Message;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.pushprovider.PushProvider;

/**
 * @author Roman Savin
 * @since 20.03.2015
 */
public abstract class OPFPushHelper {

    /**
     * If the {@code OPFPushHelper} is unregistered, it chooses a push provider with
     * the highest priority and starts the registration. Does nothing in another case.
     * The registration result can be handled via the implementation of the {@link org.onepf.opfpush.listener.EventListener}
     * interface or the extension of the {@link org.onepf.opfpush.receiver.OPFPushReceiver} class.
     * <p/>
     * The priority of providers corresponds to the order in which they was added to the
     * {@link org.onepf.opfpush.configuration.Configuration} before the initialization.
     * If you set {@code true} as an argument of {@link org.onepf.opfpush.configuration.Configuration.Builder#setSelectSystemPreferred(boolean)}
     * method, the system push provider will get the highest priority.
     *
     * @throws java.lang.IllegalStateException If {@code OPFPush} haven't been initialized.
     */
    public abstract void register();

    /**
     * If the {@code OPFPushHelper} is registered or registration is in process,
     * starts the asynchronous unregistration of the current push provider. Does nothing
     * if the {@code OPFPushHelper} has already been unregistered.
     * <p/>
     * You should rarely (if ever) need to call this method. Not only is it expensive in terms of resources,
     * but it invalidates your registration ID, which you should never change unnecessarily.
     * A better approach is to simply have your server stop sending messages.
     * Only use unregister if you want to change your sender ID.
     *
     * @throws java.lang.IllegalStateException If {@code OPFPush} haven't been initialized.
     */
    public abstract void unregister();

    @SuppressWarnings({"UnusedDeclaration", "PMD.UnusedPrivateMethod"})
    private boolean canSendMessages() {
        throw new UnsupportedOperationException("Not supported in current version");
    }

    @SuppressWarnings({"UnusedDeclaration", "PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
    private void sendMessage(@NonNull final Message message) {
        throw new UnsupportedOperationException("Not supported in current version");
    }

    /**
     * Returns the registration ID if there's the registered push provider, null otherwise.
     *
     * @return The registration ID if there's the registered push provider, null otherwise.
     */
    @Nullable
    public abstract String getRegistrationId();

    /**
     * Returns the current provider name if there's the registered push provider, null otherwise.
     *
     * @return The current provider name if there's the registered push provider, null otherwise.
     */
    @Nullable
    public abstract String getProviderName();

    /**
     * Returns the registered push provider.
     *
     * @return The registered push provider. Can be null.
     */
    @Nullable
    public abstract PushProvider getCurrentProvider();

    /**
     * Returns the {@link org.onepf.opfpush.OPFPushHelper.ReceivedMessageHandler} instance.
     * Intended for the internal use, should never be called directly.
     *
     * @return The {@link org.onepf.opfpush.OPFPushHelper.ReceivedMessageHandler} instance.
     */
    @NonNull
    public abstract ReceivedMessageHandler getReceivedMessageHandler();

    /**
     * Returns {@code true} if the {@code OPFPushHelper} is registered, false otherwise.
     *
     * @return {@code true} if the {@code OPFPushHelper} is registered, false otherwise.
     */
    public abstract boolean isRegistered();

    /**
     * Returns {@code true} if the registration operation is being performed at the moment.
     *
     * @return {@code true} if the registration operation is being performed at the moment.
     */
    public abstract boolean isRegistering();

    abstract void init(@NonNull final Configuration initialConfiguration);

    abstract boolean isInitDone();

    abstract void restartRegisterOnBoot();

    abstract void register(@NonNull final String providerName);

    abstract void unregister(@NonNull final String providerName);

    abstract void onNeedRetryRegister();

    abstract void registerNextAvailableProvider(@Nullable final String prevProviderName);

    abstract void registerPackageChangeReceiver();

    @NonNull
    abstract Settings getSettings();

    /**
     * Handles messages that were received by a push provider receiver.
     */
    public interface ReceivedMessageHandler {

        /**
         * A push provider calls this method when a new message is received.
         *
         * @param providerName The name of the provider which has received the message.
         * @param extras       The message extras.
         */
        void onMessage(@NonNull final String providerName, @Nullable final Bundle extras);

        /**
         * A push provider calls this method when the server have deleted some pending messages
         * because they were collapsible.
         *
         * @param providerName  The name of the provider which has received the message.
         * @param messagesCount Deleted messages count. If the messages count is unknown, than
         *                      the {@link org.onepf.opfpush.OPFConstants#MESSAGES_COUNT_UNKNOWN} value
         *                      is passed as an argument.
         */
        void onDeletedMessages(@NonNull final String providerName, final int messagesCount);

        /**
         * A push provider calls this method when the message about successful registration is received.
         *
         * @param providerName   The name of the provider which has been registered for pushes.
         * @param registrationId The new registration ID for the instance of your app.
         */
        void onRegistered(@NonNull final String providerName, @NonNull final String registrationId);

        /**
         * A push provider calls this method when the message about successful unregistration is received.
         *
         * @param providerName      The name of the provider which has been unregistered for pushes.
         * @param oldRegistrationId The registration ID for the instance of your app that is now unregistered.
         */
        void onUnregistered(@NonNull final String providerName,
                            @Nullable final String oldRegistrationId);

        /**
         * A push providers calls this method when a registration error is received.
         *
         * @param providerName The name of the provider which has received the registration error.
         * @param error        The instance of the occurred error.
         */
        void onRegistrationError(@NonNull final String providerName,
                                 @NonNull final PushError error);

        /**
         * A push provider calls this method when an unregistration error is received.
         *
         * @param providerName The name of the provider which has received the unregistration error.
         * @param error        The instance of the occurred error.
         */
        void onUnregistrationError(@NonNull final String providerName,
                                   @NonNull final PushError error);

        /**
         * A push provider calls this method when an error of unknown operation is received.
         * The right callback method is chosen depends on the current {@code OPFPushHelper} state.
         *
         * @param providerName The name of the provider which has received the error.
         * @param error        The instance of the occurred error.
         */
        void onError(@NonNull final String providerName, @NonNull final PushError error);
    }
}
