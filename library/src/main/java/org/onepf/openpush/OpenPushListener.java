package org.onepf.openpush;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public interface OpenPushListener {

    void onMessage(@NotNull String providerName, @Nullable Bundle extras);

    void onDeletedMessages(@NotNull String providerName, @Nullable Bundle extras);

    void onRegistered(@NotNull String providerName, @Nullable String registrationId);

    void onRegistrationError(@NotNull String providerName, int errorId);

    void onNoAvailableProvider();

    void onUnregistered(@NotNull String providerName, @Nullable String registrationId);
}
