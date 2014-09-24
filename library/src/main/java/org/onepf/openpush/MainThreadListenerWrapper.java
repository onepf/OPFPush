package org.onepf.openpush;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
* Created by krozov on 24.09.14.
*/
class MainThreadListenerWrapper implements OpenPushListener {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private final OpenPushListener mListener;

    MainThreadListenerWrapper(OpenPushListener listener) {
        mListener = listener;
    }

    @Override
    public void onMessage(@NonNull final String providerName, @Nullable final Bundle extras) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onMessage(providerName, extras);
            }
        });
    }

    @Override
    public void onDeletedMessages(@NonNull final String providerName, final int messagesCount) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onDeletedMessages(providerName, messagesCount);
            }
        });

    }

    @Override
    public void onRegistered(@NonNull final String providerName,
                             @NonNull final String registrationId) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onRegistered(providerName, registrationId);
            }
        });

    }

    @Override
    public void onRegistrationError(@NonNull final String providerName, @NonNull final Error error) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onRegistrationError(providerName, error);
            }
        });

    }

    @Override
    public void onUnregistrationError(@NonNull final String providerName,
                                      @NonNull final Error error) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onUnregistrationError(providerName, error);
            }
        });
    }

    @Override
    public void onNoAvailableProvider() {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onNoAvailableProvider();
            }
        });
    }

    @Override
    public void onUnregistered(@NonNull final String providerName,
                               @NonNull final String registrationId) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onUnregistered(providerName, registrationId);
            }
        });
    }

    @Override
    public void onProviderBecameUnavailable(@NonNull final String providerName) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                mListener.onProviderBecameUnavailable(providerName);
            }
        });
    }
}
