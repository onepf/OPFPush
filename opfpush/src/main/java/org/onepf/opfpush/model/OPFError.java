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

package org.onepf.opfpush.model;

import org.onepf.opfpush.listener.EventListener;

/**
 * Registration or unregistering operation error.
 * Divided in two categories: <b>recoverable</b> and <b>unrecoverable</b>.
 * <p/>
 * The <b>recoverable</b> error means that the service can't handle an operation
 * at the moment. The {@link org.onepf.opfpush.OPFPushHelper} retries an operation that cause
 * the recoverable error in the background.
 * <p/>
 * <b>Unrecoverable</b> error means that the error is fatal and the {@link org.onepf.opfpush.OPFPushHelper}
 * can't register this provider. In this case the {@link org.onepf.opfpush.OPFPushHelper} chooses the next
 * available provider. If there are no any other available providers,
 * the {@link EventListener#onNoAvailableProvider(android.content.Context, java.util.Map)} method will be called.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 09.09.14.
 */
public enum OPFError {

    /**
     * A service is not available at the moment. Most popular reason of this error is the internet
     * connection unavailability.
     * <p/>
     * Recoverable error.
     */
    SERVICE_NOT_AVAILABLE,

    /**
     * Invalid parameters have been sent to register provider.
     * <p/>
     * Unrecoverable error.
     */
    INVALID_PARAMETERS,

    /**
     * An invalid sender ID has been used for the registration.
     * <p/>
     * Unrecoverable error.
     */
    INVALID_SENDER,

    /**
     * The authentication failure.
     * <p/>
     * Unrecoverable error.
     */
    AUTHENTICATION_FAILED,

    /**
     * A provider specific error has occurred.
     * <p/>
     * Unrecoverable error.
     */
    UNKNOWN_ERROR
}
