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
                         BroadcastListener.ERROR_INVALID_PARAMETERS,
                         BroadcastListener.ERROR_INVALID_SENDER,
                         BroadcastListener.ERROR_SERVICE_NOT_AVAILABLE,
                         BroadcastListener.ERROR_UNKNOWN,
                         BroadcastListener.NO_ERROR,
                         BroadcastListener.ERROR_AUTHEFICATION_FAILED
                 })
                 int errorId);

    void onNoAvailableProvider();

    void onUnregistered(@NotNull String providerName, @Nullable String registrationId);

    void onHostAppRemoved(@NotNull String providerName, @NotNull String hostAppPackage);
}
