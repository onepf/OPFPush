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
 * Created by krozov on 05.09.14.
 */
public final class Result {

    @NonNull
    private final String mProviderName;
    private final String mRegistrationId;
    private final Error mErrorCode;
    private final boolean mRecoverableError;

    private Result(@NonNull String providerName,
                   @Nullable String registrationId,
                   @Nullable Error error,
                   boolean recoverableError) {
        mProviderName = providerName;
        mRegistrationId = registrationId;
        mErrorCode = error;
        mRecoverableError = recoverableError;
    }

    public static Result success(@NonNull String providerName,
                                 @NonNull String registrationId) {
        return new Result(providerName, registrationId, null, false);
    }

    public static Result error(@NonNull String providerName,
                               @NonNull Error error,
                               boolean recoverableError) {
        return new Result(providerName, null, error, recoverableError);
    }

    @Override
    public String toString() {
        if (mErrorCode == null) {
            return "RegistrationResult{" +
                    "providerName='" + mProviderName + '\'' +
                    ", registrationId='" + mRegistrationId + "\'}";
        } else {
            return "RegistrationResult{" +
                    "providerName='" + mProviderName + '\'' +
                    ", errorCode='" + mErrorCode + '\'' +
                    ", recoverableError='" + mRecoverableError + '\'' +
                    "}";
        }
    }

    public boolean isRecoverableError() {
        return mRecoverableError;
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

    public Error getErrorCode() {
        return mErrorCode;
    }
}
