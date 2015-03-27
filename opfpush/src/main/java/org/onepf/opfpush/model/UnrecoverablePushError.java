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
import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * The unrecoverable {@link org.onepf.opfpush.model.PushError}.
 * Occurs while a registration or a provider availability checking.
 *
 * @author Roman Savin
 * @since 18.03.2015
 */
public final class UnrecoverablePushError extends PushError<UnrecoverablePushError.Type> {

    @Nullable
    private Integer availabilityErrorCode;

    public UnrecoverablePushError(@NonNull final Type type,
                                  @NonNull final String providerName) {
        this(type, providerName, type.name(), null);
    }

    public UnrecoverablePushError(@NonNull final Type type,
                                  @NonNull final String providerName,
                                  @NonNull final String errorId) {
        this(type, providerName, errorId, null);
    }

    public UnrecoverablePushError(@NonNull final Type type,
                                  @NonNull final String providerName,
                                  @NonNull final Integer availabilityErrorCode) {
        this(type, providerName, type.name(), availabilityErrorCode);
    }

    public UnrecoverablePushError(@NonNull final Type type,
                                  @NonNull final String providerName,
                                  @NonNull final String errorId,
                                  @Nullable final Integer availabilityErrorCode) {
        super(type, providerName, errorId);
        this.availabilityErrorCode = availabilityErrorCode;
    }

    /**
     * Returns an availability error code, or {@code null} if a provider hasn't returned a specific availability error.
     * For GCM it corresponds to the {@code ConnectionResult} codes.
     *
     * @return The error code indicates the specific provider availability error. Can be {@code null}.
     */
    @Nullable
    public Integer getAvailabilityErrorCode() {
        return availabilityErrorCode;
    }

    @Override
    public boolean isRecoverable() {
        return false;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "UnrecoverablePushError : \n"
                        + "{\"providerName\" : \"%1$s\",\n"
                        + "\"type\" : \"%2$s\",\n"
                        + "\"originalError\":\"%3$s\",\n"
                        + "\"availabilityErrorCode\":\"%4$s\"}",
                providerName,
                type,
                originalError,
                availabilityErrorCode);
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
         * A provider specific error has occurred while a registration.
         * Look {@link #getOriginalError()} for more information.
         */
        PROVIDER_SPECIFIC_ERROR,

        /**
         * Occurred when a provider is unavailable by provider specific error.
         * Use {@link #getAvailabilityErrorCode()} to know about the reason of a provider unavailability.
         */
        AVAILABILITY_ERROR
    }
}
