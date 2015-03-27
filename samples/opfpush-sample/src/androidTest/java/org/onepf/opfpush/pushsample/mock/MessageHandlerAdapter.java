package org.onepf.opfpush.pushsample.mock;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.model.PushError;

/**
 * @author antonpp
 * @since 27.03.15
 */
public abstract class MessageHandlerAdapter implements MockReceivedMessageHandler.MessageHandlerListener {
    @Override
    public void onMessage(@NonNull String providerName, @Nullable Bundle extras) {

    }

    @Override
    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {

    }

    @Override
    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {

    }

    @Override
    public void onUnregistered(@NonNull String providerName, @Nullable String oldRegistrationId) {

    }

    @Override
    public void onRegistrationError(@NonNull String providerName, @NonNull PushError error) {

    }

    @Override
    public void onUnregistrationError(@NonNull String providerName, @NonNull PushError error) {

    }

    @Override
    public void onError(@NonNull String providerName, @NonNull PushError error) {

    }
}
