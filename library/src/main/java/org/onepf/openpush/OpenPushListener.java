package org.onepf.openpush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public class OpenPushListener {

    private final Context mContext;

    OpenPushListener(@NotNull Context context) {
        mContext = context;
    }

    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        sendBroadcast(OpenPushConstants.ACTION_MESSAGE, providerName, extras);
    }

    public void onDeletedMessages(@NotNull String providerName,
                                  @Nullable Bundle extras) {
        sendBroadcast(OpenPushConstants.ACTION_DELETED_MESSAGES, providerName, extras);
    }

    void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
        Bundle extras = new Bundle(1);
        extras.putString(OpenPushConstants.EXTRA_REGISTRATION_ID, registrationId);
        sendBroadcast(OpenPushConstants.ACTION_REGISTERED, providerName, extras);
    }

    void onRegistrationError(@NotNull String providerName, int errorId) {
        Bundle extras = new Bundle(1);
        extras.putInt(OpenPushConstants.EXTRA_ERROR_ID, errorId);
        sendBroadcast(OpenPushConstants.ACTION_REGISTRATION_ERROR, providerName, extras);
    }

    void onNoAvailableProvider() {
        sendBroadcast(OpenPushConstants.ACTION_NO_AVAILABLE_PROVIDER, null, null);
    }

    public void onUnregistered(@NotNull String providerName, @Nullable String registrationId) {
        Bundle extras = new Bundle(1);
        extras.putString(OpenPushConstants.EXTRA_REGISTRATION_ID, registrationId);
        sendBroadcast(OpenPushConstants.ACTION_UNREGISTERED, providerName, extras);
    }

    private void sendBroadcast(@NotNull String action,
                               @Nullable String providerName,
                               @Nullable Bundle extras) {
        Intent newIntent = new Intent(action);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        if (providerName != null) {
            newIntent.putExtra(OpenPushConstants.EXTRA_PROVIDER_NAME, providerName);
        }
        mContext.sendBroadcast(newIntent);
    }
}
