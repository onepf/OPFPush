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

import android.support.annotation.NonNull;

/**
 * The unrecoverable {@link org.onepf.opfpush.model.PushError}.
 *
 * @author Roman Savin
 * @since 18.03.2015
 */
public class UnrecoverablePushError extends PushError<UnrecoverablePushError.Type> {

    public UnrecoverablePushError(@NonNull final Type type,
                                  @NonNull final String providerName,
                                  @NonNull final String errorId) {
        super(type, providerName, errorId);
    }

    @Override
    public boolean isRecoverable() {
        return false;
    }

    public static enum Type implements ErrorType {

        /**
         * Invalid parameters have been sent to register provider.
         */
        INVALID_PARAMETERS,

        /**
         * An invalid sender ID has been used for the registration.
         */
        INVALID_SENDER,

        /**
         * The authentication failure.
         */
        AUTHENTICATION_FAILED,

        /**
         * A provider specific error has occurred.
         * Look {@link org.onepf.opfpush.model.UnrecoverablePushError#getErrorId()} for more information.
         */
        UNKNOWN_ERROR
    }
}
