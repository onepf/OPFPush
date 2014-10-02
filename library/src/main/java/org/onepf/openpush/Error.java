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

/**
 * Error of registration or unregistration.
 * Divided on two categories: <b>recoverable</b> and <b>non recoverable</b>.
 * <p/>
 * <b>Recoverable</b> error means that service in this moment can't handle registration
 * or unregistration and after some period of time you can try register on unregister again.
 * <b>Not recoverable</b> error means that error is fatal and you can't register this provider.
 *
 * @author Kirill Rozov
 * @since 09.09.14.
 */
public enum Error {
    /**
     * Service not available at this moment. Most popular reason of this error that no internet
     * connection available.
     * <p/>
     * Recoverable error.
     */
    SERVICE_NOT_AVAILABLE,

    /**
     * Invalid params send to register provider.
     * <p/>
     * Non recoverable error.
     */
    INVALID_PARAMETERS,

    /**
     * Invalid sender ID.
     * <p/>
     * Non recoverable error.
     */
    INVALID_SENDER,

    /**
     * Credential that you use for registration is not valid.
     * <p/>
     * Non recoverable error.
     */
    AUTHENTICATION_FAILED
}
