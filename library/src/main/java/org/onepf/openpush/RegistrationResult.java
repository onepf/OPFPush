package org.onepf.openpush;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * Created by krozov on 05.09.14.
 */
public final class RegistrationResult {

    private final String mProviderName;
    private final String mRegistrationId;
    private final int mErrorCode;
    private final boolean mRecoverableError;

    public RegistrationResult(@NotNull String providerName,
                              @NotNull String registrationId) {
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
