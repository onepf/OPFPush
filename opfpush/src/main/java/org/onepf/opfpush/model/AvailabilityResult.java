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

/**
 * Result of checking a provider availability.
 *
 * @author Roman Savin
 * @since 27.03.2015
 */
public final class AvailabilityResult {

    private boolean isAvailable;

    @Nullable
    private Integer errorCode;

    public AvailabilityResult(final boolean isAvailable) {
        this(isAvailable, null);
    }

    public AvailabilityResult(@NonNull final Integer errorCode) {
        this(false, errorCode);
    }

    public AvailabilityResult(final boolean isAvailable, @Nullable final Integer errorCode) {
        this.isAvailable = isAvailable;
        this.errorCode = errorCode;
    }

    /**
     * Returns {@code true} if a provider is available, false otherwise.
     *
     * @return {@code true} if a provider is available, false otherwise.
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Returns a specific provider error code.
     *
     * @return The availability error code, or {@code null} if a provider hasn't returned a error code.
     */
    @Nullable
    public Integer getErrorCode() {
        return errorCode;
    }
}
