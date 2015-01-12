/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfpush;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfpush.exception.OPFIllegalStateException;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.Message;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.model.State;
import org.onepf.opfpush.configuration.Backoff;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.util.ReceiverUtils;
import org.onepf.opfutils.Checkable;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.provider.Settings.Secure.ANDROID_ID;
import static org.onepf.opfpush.model.State.REGISTERED;
import static org.onepf.opfpush.model.State.REGISTERING;
import static org.onepf.opfpush.model.State.UNREGISTERED;
import static org.onepf.opfpush.model.State.UNREGISTERING;

/**
 * Main class for manage push providers.
 * <p/>
 * Before do any operations with {@code OpenPushHelper} you must call {@link #init(org.onepf.opfpush.configuration.Configuration)}.
 * <p/>
 * For start select provider for registerWithNext call {@link #register()}.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 04.09.2014
 */
public final class OPFPushHelper {

    @NonNull
    private EventListener eventListenerWrapper;

    @NonNull
    private final Context appContext;

    @Nullable
    private BroadcastReceiver packageReceiver;

    @Nullable
    private PushProvider currentProvider;

    @Nullable
    private AlarmManager alarmManager;

    private volatile Configuration configuration;

    @NonNull
    private final ReceivedMessageHandler receivedMessageHandler = new ReceivedMessageHandler();

    @NonNull
    private final Settings settings;

    @NonNull
    private final Object registrationLock = new Object();

    @NonNull
    private final Object initLock = new Object();

    @NonNull
    private final Checkable initDoneCheckable = new Checkable() {
        @Override
        public boolean check() {
            synchronized (initLock) {
                return configuration != null;
            }
        }
    };

    OPFPushHelper(@NonNull final Context context) {
        appContext = context.getApplicationContext();
        settings = new Settings(context);
    }

    /**
     * Start select push provider and registered it.
     * <p/>
     * If you want to modify current registered provider, you must call unregister() first.
     * <p/>
     *
     * @throws OPFPushException When try call this method while init not done.
     */
    public void register() {
        OPFPushLog.methodD(OPFPushHelper.class, "register");

        OPFChecks.checkInit(initDoneCheckable, true);

        synchronized (registrationLock) {
            final State state = settings.getState();
            OPFPushLog.d("Register state : " + state.toString());

            switch (state) {
                case REGISTERING:
                case REGISTERED:
                    break;
                case UNREGISTERING:
                case UNREGISTERED:
                    settings.saveState(REGISTERING);
                    if (configuration.isSelectSystemPreferred()
                            && registerSystemPreferredProvider()) {
                        return;
                    }
                    registerFirstAvailableProvider();
                    break;
            }
        }
    }

    /**
     * Unregister the application. Calling unregister() stops any messages from the server.
     * This is a not blocking call. You should rarely (if ever) need to call this method.
     * Not only is it expensive in terms of resources, but it invalidates your registration ID,
     * which you should never change unnecessarily.
     * A better approach is to simply have your server stop sending messages.
     * Only use unregister if you want to change your sender ID.
     *
     * @throws OPFPushException When try call this method while init not done.
     */
    public void unregister() {
        OPFPushLog.methodD(OPFPushHelper.class, "unregister");

        OPFChecks.checkInit(initDoneCheckable, true);

        synchronized (registrationLock) {
            final State state = settings.getState();
            OPFPushLog.i("Registration state = " + state.toString());
            if (currentProvider == null) {
                OPFPushLog.d("Current provider null");
                return;
            }

            switch (state) {
                case UNREGISTERING:
                case UNREGISTERED:
                    break;
                case REGISTERING:
                case REGISTERED:
                    settings.saveState(UNREGISTERING);
                    currentProvider.unregister();
                    break;
            }
        }
    }

    /**
     * Check can you send message in current time. This method return only if provider is registered
     * and it is implement {@link SenderPushProvider} interface.
     */
    public boolean canSendMessages() {
        return currentProvider instanceof SenderPushProvider;
    }

    /**
     * Send message to server. Before send message check that you can send messages with
     * {@link #canSendMessages()} method.
     *
     * @param message Message to send.
     * @throws OPFPushException When try send message when any provider isn't registered
     *                          or registered provider doesn't support send messages.
     */
    public void sendMessage(@NonNull final Message message) {
        synchronized (registrationLock) {
            OPFPushLog.methodD(OPFPushHelper.class, "sendMessage", message);

            if (currentProvider instanceof SenderPushProvider) {
                ((SenderPushProvider) currentProvider).send(message);
            } else if (isRegistered()) {
                throw new OPFPushException(
                        "Current provider '%s' not support send messages.",
                        currentProvider
                );
            } else {
                throw new OPFIllegalStateException("Provider not registered.");
            }
        }
    }

    @Nullable
    public String getRegistrationId() {
        if (currentProvider != null) {
            return currentProvider.getRegistrationId();
        }

        return null;
    }

    @Nullable
    public PushProvider getCurrentProvider() {
        return currentProvider;
    }

    @NonNull
    public ReceivedMessageHandler getReceivedMessageHandler() {
        OPFChecks.checkInit(initDoneCheckable, true);
        return receivedMessageHandler;
    }

    @NonNull
    @Override
    public String toString() {
        return "OpenPushHelper{"
                + "options="
                + configuration
                + ", currentProvider="
                + currentProvider
                + ", initDone="
                + isInitDone()
                + ", registered="
                + isRegistered()
                + '}';
    }

    /**
     * Init {@code OpenPushHelper}. You must call this method before do any operation.
     *
     * @param initialConfiguration Instance of {@code Options}.
     */
    @SuppressFBWarnings({"DC_DOUBLECHECK", "DC_DOUBLECHECK"})
    void init(@NonNull final Configuration initialConfiguration) {
        OPFPushLog.methodD(OPFPushHelper.class, "init", initialConfiguration);

        if (isInitDone()) {
            throw new OPFPushException("You can init OpenPushHelper only one time.");
        }

        if (this.configuration == null) {
            synchronized (initLock) {
                if (this.configuration == null) {
                    this.configuration = initialConfiguration;
                }
            }
        }

        final EventListener eventListener = configuration.getEventListener();
        final boolean isOPFReceiverRegistered = ReceiverUtils.isOPFReceiverRegistered(appContext);

        OPFPushLog.d("isOPFReceiverRegistered == " + isOPFReceiverRegistered
                + "; eventListenerWrapper == " + eventListener);
        if (isOPFReceiverRegistered && eventListener != null) {
            throw new OPFPushException("You can't register OPFReceiver and set event listener");
        } else if (!isOPFReceiverRegistered && eventListener == null) {
            throw new OPFPushException("You must register OPFReceiver or set event listener");
        }

        this.eventListenerWrapper = EventListenerWrapperCreator
                .getEventListenerWrapper(appContext, eventListener);
        restoreLastProvider();
        OPFPushLog.i("Init done.");
    }

    /**
     * Is init done and you may work with {@code OpenPushHelper}.
     *
     * @return True if init is done, else - false.
     */
    boolean isInitDone() {
        return initDoneCheckable.check();
    }

    boolean isRegistered() {
        return settings.getState() == REGISTERED;
    }

    boolean isUnregistered() {
        return settings.getState() == UNREGISTERED;
    }

    boolean isRegistering() {
        return settings.getState() == REGISTERING;
    }

    boolean isUnregistering() {
        return settings.getState() == UNREGISTERING;
    }

    void restartRegisterOnBoot() {
        OPFPushLog.methodD(OPFPushHelper.class, "restartRegisterOnBoot");

        OPFChecks.checkInit(initDoneCheckable, true);
        settings.clear();
        register();
    }

    void register(@NonNull final String providerName) {
        register(getProviderWithException(providerName));
    }

    /**
     * Call this method when device state changed and need retry registration.
     * May be call only when the helper in registered state.
     */
    void onNeedRetryRegister() {
        OPFPushLog.methodD(OPFPushHelper.class, "onNeedRetryRegister", currentProvider);
        OPFPushLog.d("Current provider : " + currentProvider);
        settings.clear();
        if (currentProvider != null) {
            currentProvider.onRegistrationInvalid();
        }
        register();
    }

    /**
     * Call this method when provider become unavailable.
     *
     * @param provider Provider that become unavailable.
     */
    void onProviderUnavailable(@NonNull final PushProvider provider) {
        OPFPushLog.methodD(OPFPushHelper.class, "onProviderUnavailable", provider);

        provider.onUnavailable();
        if (provider.equals(currentProvider)) {
            OPFPushLog.d("Unavailable provider is equals current provider");

            currentProvider = null;
            settings.clear();

            register(); //Restart registration
        }
    }

    private void postRetryRegister(@NonNull final String providerName) {
        OPFPushLog.methodD(OPFPushHelper.class, "postRetryRegister", providerName);

        final long when = System.currentTimeMillis() + configuration.getBackoff().getTryDelay();

        OPFPushLog.d("Post retry register provider '%s' at %s", providerName,
                SimpleDateFormat.getDateTimeInstance().format(new Date(when)));

        final Intent intent = new Intent(appContext, RetryBroadcastReceiver.class);
        intent.setAction(OPFConstants.ACTION_REGISTER);
        intent.putExtra(OPFConstants.EXTRA_PROVIDER_NAME, providerName);

        if (alarmManager == null) {
            alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        }
        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC, when, PendingIntent.getBroadcast(appContext, 0, intent, 0)
            );
        }
    }

    private void restoreLastProvider() {
        OPFPushLog.methodD(OPFPushHelper.class, "restoreLastProvider");

        final PushProvider lastProvider = getLastProvider();
        if (lastProvider == null) {
            OPFPushLog.d("No last provider.");
            return;
        }

        OPFPushLog.d("Try restore last provider '%s'.", lastProvider);

        if (lastProvider.isAvailable() && lastProvider.isRegistered()) {
            OPFPushLog.d("Last provider is available and registered");
            currentProvider = lastProvider;
            settings.saveState(REGISTERED);
        } else {
            OPFPushLog.d("Last provider is unavailable or unregistered");
            settings.clear();

            onProviderUnavailable(lastProvider);
        }
    }

    private boolean registerSystemPreferredProvider() {
        OPFPushLog.methodD(OPFPushHelper.class, "registerSystemPreferredProvider");

        for (PushProvider provider : configuration.getProviders()) {
            final String hostAppPackage = provider.getHostAppPackage();
            OPFPushLog.d("Provider name : " + provider.getName());
            OPFPushLog.d("Host app package : " + hostAppPackage);

            if (hostAppPackage != null) {
                if (OPFUtils.isSystemApp(appContext, hostAppPackage)
                        && provider.isAvailable()) {
                    register(provider);
                    return true;
                }
            }
        }

        OPFPushLog.d("There aren't any available system preferred providers");
        return false;
    }

    /**
     * Register first available provider.
     *
     * @return True if find provider that can be registered, otherwise false.
     */
    private boolean registerFirstAvailableProvider() {
        OPFPushLog.methodD(OPFPushHelper.class, "registerFirstAvailableProvider");

        final List<PushProvider> providers = configuration.getProviders();

        for (PushProvider provider : providers) {
            OPFPushLog.d("Provider name : " + provider.getName());

            if (provider.isAvailable()) {
                OPFPushLog.d("Provider is available");
                register(provider);
                return true;
            }
        }

        settings.saveState(UNREGISTERED);
        OPFPushLog.w("No more available providers.");
        eventListenerWrapper.onNoAvailableProvider();
        return false;
    }

    /**
     * Start register provider.
     *
     * @param provider Provider for registration.
     */
    private void register(@NonNull final PushProvider provider) {
        OPFPushLog.methodD(OPFPushHelper.class, "register", provider);
        final String regId = provider.getRegistrationId();
        if (!provider.isRegistered() || TextUtils.isEmpty(regId)) {
            provider.register();
        } else {
            receivedMessageHandler.onRegistered(provider.getName(), provider.getRegistrationId());
        }
    }

    private void unregisterPackageChangeReceiver() {
        OPFPushLog.methodD(OPFPushHelper.class, "unregisterPackageChangeReceiver");

        if (packageReceiver != null) {
            appContext.unregisterReceiver(packageReceiver);
            packageReceiver = null;
        }
    }

    /**
     * Search provider by name in {@code options} and return in.
     * If {@code} doesn't contain provider with described name return null.
     *
     * @param providerName Name of provider for search.
     * @return Provider with described name or null if nothing have found.
     */
    @Nullable
    private PushProvider getProvider(@NonNull final String providerName) {
        OPFPushLog.methodD(OPFPushHelper.class, "getProvider", providerName);

        for (PushProvider provider : configuration.getProviders()) {
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }

        OPFPushLog.d("There isn't provider with name \"" + providerName + "\"");
        return null;
    }

    /**
     * Same that {@link #getProvider(String)} but if provider not found throw exception.
     *
     * @param providerName Name of provider for search.
     * @return Provider with name {@code providerName}.
     * @throws OPFPushException When {@code PushProvider} with name {@code providerName} not found.
     */
    @NonNull
    private PushProvider getProviderWithException(@NonNull final String providerName) {
        OPFPushLog.methodD(OPFPushHelper.class, "getProviderWithException", providerName);

        final PushProvider provider = getProvider(providerName);
        if (provider == null) {
            throw new OPFPushException("Provider with name '%s' not found.", providerName);
        }
        return provider;
    }

    @Nullable
    private PushProvider getLastProvider() {
        OPFPushLog.methodD(OPFPushHelper.class, "getLastProvider");

        final String storedProviderName = settings.getLastProviderName();
        if (!TextUtils.isEmpty(storedProviderName)) {
            final PushProvider provider = getProvider(storedProviderName);
            if (provider != null) {
                return provider;
            }

            settings.saveLastProvider(null);
        }

        OPFPushLog.d("There isn't a stored provider");
        return null;
    }

    /**
     * Is used for handle received messages by broadcast receivers of concrete providers.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final class ReceivedMessageHandler {

        ReceivedMessageHandler() {
        }

        /**
         * Receiver must call this method when new message received.
         *
         * @param providerName Name of provider from what message received.
         * @param extras       Message extras.
         */
        public void onMessage(@NonNull final String providerName,
                              @Nullable final Bundle extras) {
            OPFPushLog.methodD(ReceivedMessageHandler.class, "onMessage", providerName);
            settings.saveState(REGISTERED);
            eventListenerWrapper.onMessage(providerName, extras);
        }

        /**
         * Receiver must call this method when new message deleted.
         *
         * @param providerName  Name of provider from what message deleted.
         * @param messagesCount Deleted messages count. If messages count is unknown pass -1.
         */
        public void onDeletedMessages(@NonNull final String providerName,
                                      final int messagesCount) {
            OPFPushLog.methodD(ReceivedMessageHandler.class, "onDeletedMessages",
                    providerName, messagesCount);
            settings.saveState(REGISTERED);
            eventListenerWrapper.onDeletedMessages(providerName, messagesCount);
        }

        /**
         * Receiver must call this method when a registration request succeeds.
         *
         * @param providerName   Name of provider that was registered.
         * @param registrationId The new registration ID for the instance of your app.
         */
        public void onRegistered(@NonNull final String providerName,
                                 @NonNull final String registrationId) {
            synchronized (registrationLock) {
                OPFPushLog.methodD(ReceivedMessageHandler.class, "onRegistered",
                        providerName, "registrationId"); //Don't log registration id.

                OPFPushLog.d("Successfully register provider '%s'.", providerName);

                configuration.getBackoff().reset();

                settings.saveState(REGISTERED);
                settings.saveLastAndroidId(ANDROID_ID);
                currentProvider = getProviderWithException(providerName);
                settings.saveLastProvider(currentProvider);

                eventListenerWrapper.onRegistered(providerName, registrationId);

                packageReceiver = ReceiverUtils
                        .registerPackageChangeReceiver(appContext, currentProvider);
            }
        }

        /**
         * Receiver must call this method on successful unregistration.
         *
         * @param providerName      Name of provider that was unregistered.
         * @param oldRegistrationId The registration ID for the instance of your app that is now unregistered.
         */
        public void onUnregistered(@NonNull final String providerName,
                                   @NonNull final String oldRegistrationId) {
            synchronized (registrationLock) {
                OPFPushLog.methodD(ReceivedMessageHandler.class, "onUnregistered",
                        providerName, "oldRegistrationId"); //Don't log registration id.

                OPFPushLog.d("Successfully unregister provider '%s'.", providerName);
                settings.clear();
                currentProvider = null;
                unregisterPackageChangeReceiver();
                eventListenerWrapper.onUnregistered(providerName, oldRegistrationId);
            }
        }

        /**
         * Receiver must call this method when a registration request fails.
         *
         * @param providerName Name of provider the registration of which caused the error.
         * @param error        Instance of occurred error.
         */
        public void onRegistrationError(@NonNull final String providerName,
                                        @NonNull final OPFError error) {
            synchronized (registrationLock) {
                OPFPushLog.methodD(ReceivedMessageHandler.class, "onRegistrationError", providerName, error);
                if (!isRegistered()) {
                    OPFPushLog.d("Registration state isn't REGISTERED");

                    settings.saveState(UNREGISTERED);
                    final Backoff backoff = configuration.getBackoff();
                    if (error == OPFError.SERVICE_NOT_AVAILABLE
                            && backoff.hasTries()) {
                        postRetryRegister(providerName);
                    } else {
                        backoff.reset();
                    }
                }

                eventListenerWrapper.onRegistrationError(providerName, error);
            }
        }

        /**
         * Receiver must call this method when a unregistration request fails.
         *
         * @param providerName Name of provider the unregistration of which caused the error.
         * @param error        Instance of occurred error.
         */
        public void onUnregistrationError(@NonNull final String providerName,
                                          @NonNull final OPFError error) {
            synchronized (registrationLock) {
                OPFPushLog.methodD(ReceivedMessageHandler.class, "onUnregistrationError",
                        providerName, error);

                if (isUnregistering() || isUnregistered()) {
                    settings.saveState(REGISTERED);
                }
                eventListenerWrapper.onUnregistrationError(providerName, error);
            }
        }

        /**
         * Receiver must call this method when the error occurred by unknown reason.
         *
         * @param providerName Name of provider the registration or unregistration of which caused the error.
         * @param error        Instance of occurred error.
         */
        public void onError(@NonNull final String providerName, @NonNull final OPFError error) {
            synchronized (registrationLock) {
                OPFPushLog.methodD(ReceivedMessageHandler.class, "onError", providerName, error);

                final State state = settings.getState();
                final boolean isRegistered = getProviderWithException(providerName).isRegistered();
                OPFPushLog.d("Registration state : " + state + " Provider.isRegistered == " + isRegistered);
                if (state == REGISTERING || !isRegistered) {
                    onRegistrationError(providerName, error);
                } else {
                    //state == UNREGISTERING || isRegistered
                    onUnregistrationError(providerName, error);
                }
            }
        }
    }
}
