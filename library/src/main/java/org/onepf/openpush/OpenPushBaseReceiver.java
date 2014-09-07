package org.onepf.openpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public class OpenPushBaseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (OpenPushConstants.ACTION_MESSAGE.equals(action)) {
            onMessage(intent.getStringExtra(OpenPushConstants.EXTRA_PROVIDER_NAME),
                    intent.getExtras());
        } else if (OpenPushConstants.ACTION_NO_AVAILABLE_PROVIDER.equals(action)) {
            onNoAvailableProvider();
        } else {
            final String providerName = intent.getStringExtra(OpenPushConstants.EXTRA_PROVIDER_NAME);
            if (OpenPushConstants.ACTION_REGISTERED.equals(action)) {
                onRegistered(providerName,
                        intent.getStringExtra(OpenPushConstants.EXTRA_REGISTRATION_ID));
            } else if (OpenPushConstants.ACTION_UNREGISTERED.equals(action)) {
                onUnregistered(providerName,
                        intent.getStringExtra(OpenPushConstants.EXTRA_REGISTRATION_ID));
            } else if (OpenPushConstants.ACTION_REGISTRATION_ERROR.equals(action)) {
                onRegistrationError(providerName,
                        intent.getIntExtra(OpenPushConstants.EXTRA_ERROR_ID, -1));
            } else if (OpenPushConstants.ACTION_DELETED_MESSAGES.equals(action)) {
                Bundle extras = new Bundle(intent.getExtras());
                extras.remove(OpenPushConstants.EXTRA_PROVIDER_NAME);
                onDeletedMessages(providerName, extras);
            }
        }
    }

    protected void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
    }

    protected void onDeletedMessages(@NotNull String providerName, @Nullable Bundle extras) {
    }

    protected void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
    }

    protected void onRegistrationError(@NotNull String providerName, int errorId) {
    }

    protected void onNoAvailableProvider() {
    }

    protected void onUnregistered(@NotNull String providerName, @Nullable String registrationId) {
    }
}
