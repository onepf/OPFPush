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

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 05.09.14.
 */
public final class RegistrationResult {

    @NotNull
    private final String mProviderName;

    @Nullable
    private final String mRegistrationId;

    private final int mErrorCode;
    private final boolean mRecoverableError;

    public RegistrationResult(@NotNull String providerName, @NotNull String registrationId) {
        mProviderName = providerName;
        mRegistrationId = registrationId;
        mErrorCode = OpenPushConstants.NO_ERROR;
        mRecoverableError = true;
    }

    public RegistrationResult(@NotNull String providerName,
                              @MagicConstant(intValues = {
                                      OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                      OpenPushConstants.ERROR_INVALID_SENDER,
                                      OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                      OpenPushConstants.ERROR_UNKNOWN,
                                      OpenPushConstants.ERROR_AUTHEFICATION_FAILED
                              }) int errorCode) {
        this(providerName, errorCode, true);
    }

    public RegistrationResult(@NotNull String providerName,
                              @MagicConstant(intValues = {
                                      OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                      OpenPushConstants.ERROR_INVALID_SENDER,
                                      OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                      OpenPushConstants.ERROR_UNKNOWN,
                                      OpenPushConstants.ERROR_AUTHEFICATION_FAILED
                              }) int errorCode,
                              boolean recoverableError) {
        mProviderName = providerName;
        mRegistrationId = null;
        mErrorCode = errorCode;
        mRecoverableError = recoverableError;
    }

    public boolean isRecoverableError() {
        return mRecoverableError;
    }

    @Nullable
    public String getRegistrationId() {
        return mRegistrationId;
    }

    @NotNull
    public String getProviderName() {
        return mProviderName;
    }

    public boolean isSuccess() {
        return mErrorCode == OpenPushConstants.NO_ERROR;
    }

    @MagicConstant(intValues = {
            OpenPushConstants.ERROR_INVALID_PARAMETERS,
            OpenPushConstants.ERROR_INVALID_SENDER,
            OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
            OpenPushConstants.ERROR_UNKNOWN,
            OpenPushConstants.NO_ERROR,
            OpenPushConstants.ERROR_AUTHEFICATION_FAILED
    })
    public int getErrorCode() {
        return mErrorCode;
    }
}
