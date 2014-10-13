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

import android.support.annotation.NonNull;

/**
 * Interface definition for a callback to be invoked when event
 * in {@link OPFPushHelper} is occurred.
 *
 * @author Kirill Rozov
 * @since 07.09.14.
 */
public interface EventListener {

    /**
     * Provider registered successfully.
     *
     * @param providerName   Name of registered provider.
     * @param registrationId Registration id for push notification.
     */
    void onRegistered(@NonNull String providerName, @NonNull String registrationId);

    /**
     * Provider registration failed. Provider can continue try to register
     * with exponential backoff (is {@code error} is recoverable) or can try to register next.
     *
     * @param providerName Name of provider in what error occur.
     * @param error        Occurred error
     */
    void onRegistrationError(@NonNull String providerName, @NonNull Error error);

    /**
     * Provider unregistration failed. Provider can continue try to unregister
     * with exponential backoff (is {@code error} is recoverable).
     *
     * @param providerName Name of provider in what error occur.
     * @param error        Occurred error
     */
    void onUnregistrationError(@NonNull String providerName, @NonNull Error error);

    /**
     * {@code OpenPushHelper} can't find any available provider for register push.
     */
    void onNoAvailableProvider();

    /**
     * Provider unregistered successfully.
     *
     * @param providerName   Name of unregistered provider.
     * @param registrationId Old registration id for push notification.
     */
    void onUnregistered(@NonNull String providerName, @NonNull String registrationId);

    /**
     * Provider went to unavailable state. Is most case reason ot this is removing
     * host application of the provider.
     *
     * @param providerName Name of provider.
     */
    void onProviderBecameUnavailable(@NonNull String providerName);
}
