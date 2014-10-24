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

/**
 * Error in registration or unregistration.
 * Divided into two categories: <b>recoverable</b> and <b>nonrecoverable</b>.
 * <p/>
 * <b>Recoverable</b> error: service can't handle registration
 * or unregistration, you can try register on unregister later again.
 * <p/>
 * <b>Not recoverable</b> error: error is fatal, you can't register this provider.
 *
 * @author Kirill Rozov
 * @since 09.09.14.
 */
public enum Error {
    /**
     * Service not available. E.g. no internet connections.
     * <p/>
     * Recoverable error.
     */
    SERVICE_NOT_AVAILABLE,

    /**
     * Invalid params were sent to a register provider.
     * <p/>
     * Nonrecoverable error.
     */
    INVALID_PARAMETERS,

    /**
     * Invalid sender ID.
     * <p/>
     * Nonrecoverable error.
     */
    INVALID_SENDER,

    /**
     * Credentials for registration are not valid.
     * <p/>
     * Nonrecoverable error.
     */
    AUTHENTICATION_FAILED
}
