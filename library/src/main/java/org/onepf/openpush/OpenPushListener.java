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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public interface OpenPushListener {

    void onMessage(@NonNull String providerName, @Nullable Bundle extras);

    void onDeletedMessages(@NonNull String providerName, int messagesCount);

    void onRegistered(@NonNull String providerName, @NonNull String registrationId);

    void onRegistrationError(@NonNull String providerName, @NonNull Error error);

    void onUnregistrationError(@NonNull String providerName, @NonNull Error error);

    void onNoAvailableProvider();

    void onUnregistered(@NonNull String providerName, @NonNull String registrationId);

    void onProviderBecameUnavailable(@NonNull String providerName);
}
