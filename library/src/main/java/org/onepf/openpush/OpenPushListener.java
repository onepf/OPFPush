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

package org.onepf.openpush;

import android.os.Bundle;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public interface OpenPushListener {

    void onMessage(@NotNull String providerName, @Nullable Bundle extras);

    void onDeletedMessages(@NotNull String providerName, int messagesCount);

    void onRegistered(@NotNull String providerName, @Nullable String registrationId);

    void onRegistrationError(@NotNull String providerName,
                             @MagicConstant(intValues = {
                                     OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                     OpenPushConstants.ERROR_INVALID_SENDER,
                                     OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                     OpenPushConstants.ERROR_UNKNOWN,
                                     OpenPushConstants.NO_ERROR,
                                     OpenPushConstants.ERROR_AUTHEFICATION_FAILED
                             })
                             int errorId);

    void onUnregistrationError(@NotNull String providerName,
                             @MagicConstant(intValues = {
                                     OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                     OpenPushConstants.ERROR_INVALID_SENDER,
                                     OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                     OpenPushConstants.ERROR_UNKNOWN,
                                     OpenPushConstants.NO_ERROR,
                                     OpenPushConstants.ERROR_AUTHEFICATION_FAILED
                             })
                             int errorId);

    void onNoAvailableProvider();

    void onUnregistered(@NotNull String providerName, @Nullable String registrationId);

    void onHostAppRemoved(@NotNull String providerName, @NotNull String hostAppPackage);
}
