package org.onepf.openpush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public class BroadcastOpenPushListener implements OpenPushListener {

    private final Context mContext;

    public BroadcastOpenPushListener(@NotNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        sendBroadcast(OpenPushConstants.ACTION_MESSAGE, providerName, extras);
    }

    @Override
    public void onDeletedMessages(@NotNull String providerName,
                                  @Nullable Bundle extras) {
        sendBroadcast(OpenPushConstants.ACTION_DELETED_MESSAGES, providerName, extras);
    }

    @Override
    public void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
        Bundle extras = new Bundle(1);
        extras.putString(OpenPushConstants.EXTRA_REGISTRATION_ID, registrationId);
        sendBroadcast(OpenPushConstants.ACTION_REGISTERED, providerName, extras);
    }

    @Override
    public void onError(@NotNull String providerName, int errorId) {
        Bundle extras = new Bundle(1);
        extras.putInt(OpenPushConstants.EXTRA_ERROR_ID, errorId);
        sendBroadcast(OpenPushConstants.ACTION_ERROR, providerName, extras);
    }

    @Override
    public void onNoAvailableProvider() {
        sendBroadcast(OpenPushConstants.ACTION_NO_AVAILABLE_PROVIDER, null, null);
    }

    @Override
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

    @Override
    public void onHostAppRemoved(@NotNull String providerName, @NotNull String hostAppPackage) {
        Bundle extras = new Bundle(1);
        extras.putString(OpenPushConstants.EXTRA_HOST_APP_PACKAGE, hostAppPackage);
        sendBroadcast(OpenPushConstants.ACTION_HOST_APP_REMOVED, providerName, extras);
    }
}
