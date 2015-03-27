package org.onepf.opfpush.pushsample.mock;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.model.PushError;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author antonpp
 * @since 27.03.15
 */
public class MockReceivedMessageHandler implements OPFPushHelper.ReceivedMessageHandler {

    @NonNull
    private final OPFPushHelper.ReceivedMessageHandler realReceivedMessageHandler;

    @NonNull
    private final MessageHandlerListener listener;

    public MockReceivedMessageHandler(@NonNull final OPFPushHelper helper, @NonNull final MessageHandlerListener listener) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> receivedMessageHandlerImplClass = helper.getClass().getDeclaredClasses()[0];
        Constructor<?> ctor = receivedMessageHandlerImplClass.getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        realReceivedMessageHandler = (OPFPushHelper.ReceivedMessageHandler) ctor.newInstance(helper);

        this.listener = listener;
    }

    @Override
    public void onMessage(@NonNull String providerName, @Nullable Bundle extras) {
        listener.onMessage(providerName, extras);
        realReceivedMessageHandler.onMessage(providerName, extras);
    }

    @Override
    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {
        listener.onDeletedMessages(providerName, messagesCount);
        realReceivedMessageHandler.onDeletedMessages(providerName, messagesCount);
    }

    @Override
    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
        listener.onRegistered(providerName, registrationId);
        realReceivedMessageHandler.onRegistered(providerName, registrationId);
    }

    @Override
    public void onUnregistered(@NonNull String providerName, @Nullable String oldRegistrationId) {
        listener.onUnregistered(providerName, oldRegistrationId);
        realReceivedMessageHandler.onUnregistered(providerName, oldRegistrationId);
    }

    @Override
    public void onRegistrationError(@NonNull String providerName, @NonNull PushError error) {
        listener.onRegistrationError(providerName, error);
        realReceivedMessageHandler.onRegistrationError(providerName, error);
    }

    @Override
    public void onUnregistrationError(@NonNull String providerName, @NonNull PushError error) {
        listener.onUnregistrationError(providerName, error);
        realReceivedMessageHandler.onUnregistrationError(providerName, error);
    }

    @Override
    public void onError(@NonNull String providerName, @NonNull PushError error) {
        listener.onError(providerName, error);
        realReceivedMessageHandler.onError(providerName, error);
    }

    public interface MessageHandlerListener {
        void onMessage(@NonNull String providerName, @Nullable Bundle extras);
        void onDeletedMessages(@NonNull String providerName, int messagesCount);
        void onRegistered(@NonNull String providerName, @NonNull String registrationId);
        void onUnregistered(@NonNull String providerName, @Nullable String oldRegistrationId);
        void onRegistrationError(@NonNull String providerName, @NonNull PushError error);
        void onUnregistrationError(@NonNull String providerName, @NonNull PushError error);
        void onError(@NonNull String providerName, @NonNull PushError error);
    }

    public static void addListenerToHelper(OPFPushHelper helper, MessageHandlerListener listener) throws Exception {
        Field field;
        field = helper.getClass().getDeclaredField("receivedMessageHandler");
        field.setAccessible(true);
        field.set(helper, new MockReceivedMessageHandler(helper, listener));
    }
}
