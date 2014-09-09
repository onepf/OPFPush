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
        mErrorCode = BroadcastListener.NO_ERROR;
        mRecoverableError = true;
    }

    public RegistrationResult(@NotNull String providerName,
                              @MagicConstant(intValues = {
                                      BroadcastListener.ERROR_INVALID_PARAMETERS,
                                      BroadcastListener.ERROR_INVALID_SENDER,
                                      BroadcastListener.ERROR_SERVICE_NOT_AVAILABLE,
                                      BroadcastListener.ERROR_UNKNOWN,
                                      BroadcastListener.ERROR_AUTHEFICATION_FAILED
                              }) int errorCode) {
        this(providerName, errorCode, true);
    }

    public RegistrationResult(@NotNull String providerName,
                              @MagicConstant(intValues = {
                                      BroadcastListener.ERROR_INVALID_PARAMETERS,
                                      BroadcastListener.ERROR_INVALID_SENDER,
                                      BroadcastListener.ERROR_SERVICE_NOT_AVAILABLE,
                                      BroadcastListener.ERROR_UNKNOWN,
                                      BroadcastListener.ERROR_AUTHEFICATION_FAILED
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

    @NotNull
    public String getRegistrationId() {
        return mRegistrationId;
    }

    @NotNull
    public String getProviderName() {
        return mProviderName;
    }

    public boolean isSuccess() {
        return mErrorCode == BroadcastListener.NO_ERROR;
    }

    @MagicConstant(intValues = {
            BroadcastListener.ERROR_INVALID_PARAMETERS,
            BroadcastListener.ERROR_INVALID_SENDER,
            BroadcastListener.ERROR_SERVICE_NOT_AVAILABLE,
            BroadcastListener.ERROR_UNKNOWN,
            BroadcastListener.NO_ERROR,
            BroadcastListener.ERROR_AUTHEFICATION_FAILED
    })
    public int getErrorCode() {
        return mErrorCode;
    }
}
