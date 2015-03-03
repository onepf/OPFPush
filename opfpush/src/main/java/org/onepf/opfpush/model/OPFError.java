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
 * Error of registration or unregistration operation.
 * Divided on two categories: <b>recoverable</b> and <b>unrecoverable</b>.
 * <p/>
 * <b>Recoverable</b> error means that service can't handle registration or unregistration
 * at the moment. {@link org.onepf.opfpush.OPFPushHelper} retries operation that cause
 * the recoverable error in the background.
 * <p/>
 * <b>Unrecoverable</b> error means that error is fatal and {@link org.onepf.opfpush.OPFPushHelper}
 * can't register this provider. In this case {@link org.onepf.opfpush.OPFPushHelper} chooses next
 * available provider. If there aren't anymore available providers,
 * {@link EventListener#onNoAvailableProvider(android.content.Context, java.util.Map)} method will be called.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 09.09.14.
 */
public enum OPFError {

    /**
     * Service not available at the moment. Most popular reason of this error is internet
     * connection unavailability.
     * <p/>
     * Recoverable error.
     */
    SERVICE_NOT_AVAILABLE,

    /**
     * Invalid params send to register provider.
     * <p/>
     * Unrecoverable error.
     */
    INVALID_PARAMETERS,

    /**
     * Invalid sender ID.
     * <p/>
     * Unrecoverable error.
     */
    INVALID_SENDER,

    /**
     * Authentication failure.
     * <p/>
     * Unrecoverable error.
     */
    AUTHENTICATION_FAILED,

    /**
     * Some provider specific error has been occurred.
     * <p/>
     * Unrecoverable error.
     */
    UNKNOWN_ERROR
}
