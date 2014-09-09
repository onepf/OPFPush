package org.onepf.openpush;

import android.os.Bundle;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public interface OpenPushListener {

    void onMessage(@NotNull String providerName, @Nullable Bundle extras);

    void onDeletedMessages(@NotNull String providerName, @Nullable Bundle extras);

    void onRegistered(@NotNull String providerName, @Nullable String registrationId);

    void onError(@NotNull String providerName,
                 @MagicConstant(intValues = {
                         OpenPushConstants.ERROR_INVALID_PARAMETERS,
                         OpenPushConstants.ERROR_INVALID_SENDER,
                         OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                         OpenPushConstants.ERROR_UNKNOWN,
                         OpenPushConstants.NO_ERROR,
                         OpenPushConstants.ERROR_AUTHEFICATION_FAILED
                 })
                 int errorId);

    void onNoAvailableProvider();

    void onUnregistered(@NotNull String providerName, @Nullable String registrationId);

    void onHostAppRemoved(@NotNull String providerName, @NotNull String hostAppPackage);
}
