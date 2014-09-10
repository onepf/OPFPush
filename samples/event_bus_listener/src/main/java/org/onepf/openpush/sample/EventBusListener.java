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

package org.onepf.openpush.sample;

import android.os.Bundle;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.OpenPushConstants;
import org.onepf.openpush.OpenPushListener;
import org.onepf.openpush.sample.event.DeleteMessageEvent;
import org.onepf.openpush.sample.event.HostAppRemovedEvent;
import org.onepf.openpush.sample.event.MessageEvent;
import org.onepf.openpush.sample.event.NoAvailableProviderEvent;
import org.onepf.openpush.sample.event.RegisteredEvent;
import org.onepf.openpush.sample.event.RegistrationErrorEvent;
import org.onepf.openpush.sample.event.UnregisteredEvent;
import org.onepf.openpush.sample.event.UnregistrationErrorEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by krozov on 07.09.14.
 */
public class EventBusListener implements OpenPushListener {

    public EventBusListener() {
    }

    @Override
    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        EventBus.getDefault().post(new MessageEvent(providerName, extras));
    }

    @Override
    public void onDeletedMessages(@NotNull String providerName, int messagesCount) {
        EventBus.getDefault().post(new DeleteMessageEvent(providerName, messagesCount));
    }

    @Override
    public void onRegistered(@NotNull String providerName, @NotNull String registrationId) {
        EventBus.getDefault().post(new RegisteredEvent(providerName, registrationId));
    }

    @Override
    public void onRegistrationError(@NotNull String providerName,
                                    @MagicConstant(intValues = {
                                            OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                            OpenPushConstants.ERROR_INVALID_SENDER,
                                            OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                            OpenPushConstants.ERROR_UNKNOWN,
                                            OpenPushConstants.NO_ERROR,
                                            OpenPushConstants.ERROR_AUTHEFICATION_FAILED})
                                    int errorId) {
        EventBus.getDefault().post(new RegistrationErrorEvent(providerName, errorId));
    }

    @Override
    public void onNoAvailableProvider() {
        EventBus.getDefault().post(new NoAvailableProviderEvent());
    }

    @Override
    public void onUnregistered(@NotNull String providerName, @NotNull String registrationId) {
        EventBus.getDefault().post(new UnregisteredEvent(providerName, registrationId));
    }

    @Override
    public void onUnregistrationError(@NotNull String providerName,
                                      @MagicConstant(intValues = {
                                              OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                              OpenPushConstants.ERROR_INVALID_SENDER,
                                              OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                              OpenPushConstants.ERROR_UNKNOWN,
                                              OpenPushConstants.NO_ERROR,
                                              OpenPushConstants.ERROR_AUTHEFICATION_FAILED})
                                      int errorId) {
        EventBus.getDefault().post(new UnregistrationErrorEvent(providerName, errorId));
    }

    @Override
    public void onHostAppRemoved(@NotNull String providerName, @NotNull String hostAppPackage) {
        EventBus.getDefault().post(new HostAppRemovedEvent(providerName, hostAppPackage));
    }
}
