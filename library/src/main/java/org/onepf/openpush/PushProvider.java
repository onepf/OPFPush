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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public interface PushProvider {
    /**
     * Initiate registration for the current application.
     * For start registration call {@link OpenPushHelper#register()}.
     * <p/>
     * <b>Don't call this method manually.</b>
     */
    void register();

    /**
     * Unregister the application.
     * For start registration call {@link OpenPushHelper#unregister()}.
     * <p/>
     * <b>Don't call this method manually.</b>
     */
    void unregister();

    /**
     * Checks whether the provider is available.
     */
    boolean isAvailable();

    /**
     * Checks whether the application was successfully registered on the service.
     */
    boolean isRegistered();

    /**
     * Gets the registration id.
     */
    @Nullable
    String getRegistrationId();

    /**
     * Returns the name of the provider. Always not null value.
     */
    @NotNull
    String getName();

    /**
     * Get package of application that contains API of the provider.
     * Usually, this is store application.
     *
     * @return Host application package.
     */
    @NotNull
    String getHostAppPackage();

    /**
     * Must be called when your application is done using PushProvider, to release internal resources.
     * {@link OpenPushHelper} call it after successfully unregister.
     */
    void close();

    /**
     * Verify does application manifest contains all needed permissions.
     *
     * @return {@code true} if all required permissions described in manifest, else {@code false}.
     */
    boolean checkManifest();

    /**
     * Callback method, that called when application state change, like update to new version,
     * or system state changed, like update firmware to a newer version and Android ID changed.
     * <p/>
     * When this method call you registration is invalid and you need reset all data associated with
     * previous registration data.
     */
    //TODO Think about method name.
    void onAppStateChanged();

    void onUnavailable();
}
