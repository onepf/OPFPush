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
import android.support.annotation.Nullable;

import org.onepf.opfpush.exception.OPFPushException;

/**
 * {@code PushProvider} represent provider for push notification from server to client app.
 * <p/>
 * The {@link #register()} and {@link #unregister()} methods intended for internal use, should never be called directly.
 * Use {@link OPFPushHelper#register()} or {@link OPFPushHelper#unregister()} to start registration
 * or unregistration.
 *
 * @author Anton Rutkevich, Alexey Vitenko, Kirill Rozov
 * @since 14.05.14
 */
public interface PushProvider {
    /**
     * Initiate registration of the provider. Must be async.
     * To start registration call {@link OPFPushHelper#register()}.
     * <p/>
     * Intended for internal use, should never be called directly.
     */
    void register();

    /**
     * Unregister the provider from receive push notification. Must be async.
     * To start registration call {@link OPFPushHelper#unregister()}.
     * <p/>
     * Intended for internal use, should never be called directly.
     */
    void unregister();

    /**
     * Checks whether the provider is available.
     *
     * @return {@code true} if the provider is available, otherwise false.
     */
    boolean isAvailable();

    /**
     * Checks whether the application was successfully registered on the service.
     *
     * @return {@code true} if the application was successfully registered on the service, otherwise false.
     */
    boolean isRegistered();

    /**
     * Returns the registration ID or null if provider isn't registered.
     *
     * @return The registration ID or null if provider isn't registered.
     */
    @Nullable
    String getRegistrationId();

    /**
     * Returns the name of the provider. Must be unique for all providers.
     *
     * @return The name of the provider.
     */
    @NonNull
    String getName();

    /**
     * Returns the package of the application that contains API of the provider.
     * Usually, this is a store application.
     *
     * @return The package of the application that contains API of the provider.
     */
    @Nullable
    String getHostAppPackage();

    /**
     * Verify that application manifest contains all needed permissions.
     *
     * @return {@code true} If all required permissions described in manifest, else {@code false}.
     * @throws OPFPushException If AndroidManifest.xml file doesn't contain all
     *                          required permissions for provider.
     */
    boolean checkManifest();

    /**
     * Callback method, that called when the application state change, like update to new version,
     * or system state changed, like update firmware to a newer version.
     * <p/>
     * If this method is called, your registration becomes invalid
     * and you have to reset all saved registration data.
     */
    void onRegistrationInvalid();

    /**
     * Callback method for notify that the provider became unavailable.
     * In this method you have to reset all saved registration data.
     */
    void onUnavailable();
}
