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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Result of registration or unregistration. Use it to communicate between provider and
 * {@code OpenPushHelper} to notify about operations progress. For successfully ended operations
 * create instance with {@link #success(String, String, org.onepf.openpush.Result.Type)}, for error -
 * {@link #error(String, Error, org.onepf.openpush.Result.Type)}.
 *
 * @author Kirill Rozov
 * @since 05.09.14.
 */
public final class Result {

    @NonNull
    private final String mProviderName;
    private final String mRegistrationId;
    private final Error mErrorCode;;
    private final Type mType;

    private Result(@NonNull String providerName,
                   @Nullable String registrationId,
                   @Nullable Error error,
                   Type type) {
        mProviderName = providerName;
        mRegistrationId = registrationId;
        mErrorCode = error;
        mType = type;
    }

    public static Result success(@NonNull String providerName,
                                 @NonNull String registrationId,
                                 @NonNull Type type) {
        return new Result(providerName, registrationId, null, type);
    }

    public static Result error(@NonNull String providerName,
                               @NonNull Error error,
                               @NonNull Type type) {
        return new Result(providerName, null, error, type);
    }

    @Override
    public String toString() {
        if (mErrorCode == null) {
            return "Success{" +
                    "providerName='" + mProviderName + '\'' +
                    ", registrationId='" + mRegistrationId + '\'' +
                    ", type=" + mType + '\'' +
                    '}';
        } else {
            return "Error{" +
                    "providerName='" + mProviderName + '\'' +
                    ", errorCode='" + mErrorCode + '\'' +
                    ", type=" + mType + '\'' +
                    '}';
        }
    }

    @Nullable
    public String getRegistrationId() {
        return mRegistrationId;
    }

    @NonNull
    public String getProviderName() {
        return mProviderName;
    }

    public boolean isSuccess() {
        return mErrorCode == null;
    }

    /**
     * @return null for success result, otherwise - not null error.
     * @see #isSuccess()
     */
    @Nullable
    public Error getError() {
        return mErrorCode;
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    /**
     * Type of result operation.
     */
    public static enum Type {
        /**
         * Use if you don't know exactly what is result is registration or unregistration.
         */
        UNKNOWN,
        REGISTRATION,
        UNREGISTRATION
    }
}
