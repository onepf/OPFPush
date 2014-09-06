package org.onepf.openpush;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * Created by krozov on 05.09.14.
 */
public final class ProviderRegistrationResult {

    private final String mProviderName;
    private final String mRegistrationId;
    private final int mErrorCode;

    public ProviderRegistrationResult(@NotNull String providerName,
                                      @NotNull String registrationId) {
        mProviderName = providerName;
        mRegistrationId = registrationId;
        mErrorCode = OpenPushConstants.NO_ERROR;
    }

    public ProviderRegistrationResult(@NotNull String providerName,
                                      @MagicConstant(intValues = {
                                              OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                              OpenPushConstants.ERROR_INVALID_SENDER,
                                              OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                              OpenPushConstants.ERROR_UNKNOWN,
                                              OpenPushConstants.ERROR_AUTHEFICATION_FAILED
                                      }) int errorCode) {
        mProviderName = providerName;
        mRegistrationId = null;
        mErrorCode = errorCode;
    }

    public String getRegistrationId() {
        return mRegistrationId;
    }

    @NotNull
    public String getProviderName() {
        return mProviderName;
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
