/*******************************************************************************
 * Copyright 2014 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

package org.onepf.openpush;

import org.jetbrains.annotations.NotNull;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public interface PushProvider {
    /**
     * Initiate registration for the current application.
     */
    void register();

    /**
     * Unregister the application.
     */
    void unregister();

    /**
     * Checks whether the provider is available
     */
    boolean isAvailable();

    /**
     * Checks whether the application was successfully registered on the service.
     */
    boolean isRegistered();

    /**
     * Gets the registration id.
     */
    String getRegistrationId();

    /**
     * Returns the name of the provider.
     */
    @NotNull
    String getName();

    /**
     * Get package of application that contains API for this provider.
     * Usually, this is store application.
     *
     * @return Host application package.
     */
    @NotNull
    String getHostAppPackage();
}
