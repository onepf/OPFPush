/*
 * Copyright 2012-2015 One Platform Foundation
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
 * The recoverable {@link org.onepf.opfpush.model.PushError}.
 * <p/>
 * Intended for the internal use, should never be called directly.
 *
 * @author Roman Savin
 * @since 18.03.2015
 */
public final class RecoverablePushError extends PushError<RecoverablePushError.Type> {

    public RecoverablePushError(@NonNull final Type type,
                                @NonNull final String providerName) {
        this(type, providerName, type.name());
    }

    public RecoverablePushError(@NonNull final Type type,
                                @NonNull final String providerName,
                                @NonNull final String errorId) {
        super(type, providerName, errorId);
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }

    public enum Type implements ErrorType {

        /**
         * A service is not available at the moment. Most popular reason of this error is the internet
         * connection unavailability.
         */
        SERVICE_NOT_AVAILABLE,

        /**
         * Occurs when try the registration while the unregistering is being performed.
         */
        UNREGISTERING_PERFORMING,

        /**
         * Occurs when try the unregistering while the registration is being performed.
         */
        REGISTERING_PERFORMING
    }
}
