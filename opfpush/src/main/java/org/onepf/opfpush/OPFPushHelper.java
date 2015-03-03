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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfpush.backoff.InfinityExponentialBackoffManager;
import org.onepf.opfpush.backoff.RetryManager;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.Message;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.model.State;
import org.onepf.opfpush.util.ReceiverUtils;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;
import org.onepf.opfutils.exception.InitException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.provider.Settings.Secure;
import static android.provider.Settings.Secure.ANDROID_ID;
import static org.onepf.opfpush.model.OPFError.SERVICE_NOT_AVAILABLE;
import static org.onepf.opfpush.model.Operation.REGISTER;
import static org.onepf.opfpush.model.Operation.UNREGISTER;
import static org.onepf.opfpush.model.State.REGISTERED;
import static org.onepf.opfpush.model.State.REGISTERING;
import static org.onepf.opfpush.model.State.UNREGISTERED;

/**
 * A helper class to manage push providers.
 * <p/>
 * Use {@link #register()} to start a registration and {@link #unregister()} for start an unregistration.
 * Registration and unregistration operations are asynchronous. You can handle results of these operations
 * via implementation of {@link org.onepf.opfpush.listener.EventListener} interface or extension of
 * {@link org.onepf.opfpush.OPFPushReceiver} class.
 * <p/>
 * You must initialize {@link org.onepf.opfpush.OPFPush} class before start using {@code OPFPushHelper}.
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

    @NonNull
    private List<PushProvider> sortedProvidersList;

    @NonNull
    private Map<String, OPFError> registerProviderErrors = new HashMap<>();

    private volatile Configuration configuration;

    @NonNull
    private final ReceivedMessageHandler receivedMessageHandler = new ReceivedMessageHandler();

    @NonNull
    private final Settings settings;

    @NonNull
    private final RetryManager retryManager;

    @NonNull
    private final Object registrationLock = new Object();

    @NonNull
    private final Object initLock = new Object();

    OPFPushHelper(@NonNull final Context context) {
        appContext = context.getApplicationContext();
        settings = Settings.getInstance(context);
        retryManager = RetryManager.getInstance(context,
                InfinityExponentialBackoffManager.getInstance());
    }

    /**
     * If {@code OPFPushHelper} is unregistered, it chooses push provider with highest priority and starts registration.
     * Does nothing in another case.
     * Registration result can be handled via implementation of {@link org.onepf.opfpush.listener.EventListener}
     * interface or extension of {@link org.onepf.opfpush.OPFPushReceiver} class.
     * <p/>
     * The priority of providers corresponds to the order in which they was added to the {@link org.onepf.opfpush.configuration.Configuration}
     * before initialization.
     * If you set {@code true} as argument of {@link org.onepf.opfpush.configuration.Configuration.Builder#setSelectSystemPreferred(boolean)}
     * method, the system push provider will get the highest priority.
     *
     * @throws org.onepf.opfutils.exception.InitException if {@link org.onepf.opfpush.OPFPush} isn't initialized.
     */
    public void register() {
        OPFLog.methodD();

        checkInit(true);
        synchronized (registrationLock) {
            final State state = settings.getState();
            OPFLog.d("Register state : " + state.toString());
            switch (state) {
                case REGISTERED:
                    registerPackageChangeReceiver();
                    break;
                case REGISTERING:
                    break;
                case UNREGISTERED:
                    settings.saveState(REGISTERING);
                    registerFirstAvailableProvider();
                    break;
            }
        }
    }

    /**
     * If {@code OPFPushHelper} is registered or registration is in process,
     * starts asynchronous unregistration of current push provider. Does nothing if {@code OPFPushHelper}
     * has already been unregistered.
     * <p/>
     * You should rarely (if ever) need to call this method. Not only is it expensive in terms of resources,
     * but it invalidates your registration ID, which you should never change unnecessarily.
     * A better approach is to simply have your server stop sending messages.
     * Only use unregister if you want to change your sender ID.
     *
     * @throws org.onepf.opfutils.exception.InitException If {@link org.onepf.opfpush.OPFPush} isn't initialized.
     */
    public void unregister() {
        OPFLog.methodD();

        checkInit(true);
        synchronized (registrationLock) {
            final State state = settings.getState();
            OPFLog.i("Registration state = " + state.toString());
            if (currentProvider == null) {
                OPFLog.w("Current provider null");
                return;
            }
            final String providerName = currentProvider.getName();
            final boolean isCurrentProviderRegistered = currentProvider.isRegistered();

            if (state == REGISTERING || state == REGISTERED || isCurrentProviderRegistered) {
                retryManager.cancelRetryAllOperations(providerName);

                final String oldRegistrationId = currentProvider.getRegistrationId();
                currentProvider.unregister();
                fakeOnUnregistered(providerName, oldRegistrationId);
            } else {
                OPFLog.w("Unregistration wasn't performed because already unregistered.");
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private boolean canSendMessages() {
        throw new UnsupportedOperationException("Not supported in current version");
        /*return currentProvider instanceof SenderPushProvider;*/
    }

    @SuppressWarnings("UnusedDeclaration")
    private void sendMessage(@NonNull final Message message) {
        throw new UnsupportedOperationException("Not supported in current version");
        /*synchronized (registrationLock) {
            OPFLog.methodD(OPFPushHelper.class, "sendMessage", message);

            if (currentProvider instanceof SenderPushProvider) {
                ((SenderPushProvider) currentProvider).send(message);
            } else if (isRegistered()) {
                throw new OPFPushException(
                        "Current provider '%s' not support send messages.",
                        currentProvider
                );
            } else {
                throw new IllegalStateException("Provider not registered.");
            }
        }*/
    }

    /**
     * Returns the registration ID if there's registered push provider, null otherwise.
     *
     * @return The registration ID if there's registered push provider, null otherwise.
     */
    @Nullable
    public String getRegistrationId() {
        if (currentProvider != null) {
            return currentProvider.getRegistrationId();
        }

        return null;
    }

    /**
     * Returns the current provider name if there's registered push provider, null otherwise.
     *
     * @return The current provider name if there's registered push provider, null otherwise.
     */
    @Nullable
    public String getProviderName() {
        if (currentProvider != null) {
            return currentProvider.getName();
        }

        return null;
    }

    /**
     * Returns the registered push provider.
     *
     * @return The registered push provider. Can be null.
     */
    @Nullable
    public PushProvider getCurrentProvider() {
        return currentProvider;
    }

    /**
     * Returns the {@link org.onepf.opfpush.OPFPushHelper.ReceivedMessageHandler} instance.
     * Intended for internal use, should never be called directly.
     *
     * @return The {@link org.onepf.opfpush.OPFPushHelper.ReceivedMessageHandler} instance.
     */
    @NonNull
    public ReceivedMessageHandler getReceivedMessageHandler() {
        checkInit(true);
        return receivedMessageHandler;
    }

    /**
     * Returns {@code true} if {@code OPFPushHelper} is registered, false otherwise.
     *
     * @return {@code true} if {@code OPFPushHelper} is registered, false otherwise.
     */
    public boolean isRegistered() {
        return settings.getState() == REGISTERED;
    }

    /**
     * Returns {@code true} if registration operation is being performed at the moment.
     *
     * @return {@code true} if registration operation is being performed at the moment.
     */
    public boolean isRegistering() {
        return settings.getState() == REGISTERING;
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

    @SuppressFBWarnings({"DC_DOUBLECHECK", "DC_DOUBLECHECK"})
    void init(@NonNull final Configuration initialConfiguration) {
        OPFLog.methodD(initialConfiguration);

        if (this.configuration == null) {
            synchronized (initLock) {
                if (this.configuration == null) {
                    this.configuration = initialConfiguration;
                }
            }
        }

        final EventListener eventListener = configuration.getEventListener();
        final boolean isOPFReceiverRegistered = ReceiverUtils.isOPFReceiverRegistered(appContext);

        OPFLog.d("isOPFReceiverRegistered == " + isOPFReceiverRegistered
                + "; eventListenerWrapper == " + eventListener);
        if (isOPFReceiverRegistered && eventListener != null) {
            throw new OPFPushException("You can't register OPFReceiver and set event listener");
        } else if (!isOPFReceiverRegistered && eventListener == null) {
            throw new OPFPushException("You must register OPFReceiver or set event listener");
        }

        initSortedProviderList();
        this.eventListenerWrapper = EventListenerWrapperCreator
                .getEventListenerWrapper(eventListener);
        restoreLastProvider();
        OPFLog.i("Init done.");
    }

    void checkInit(final boolean needInit) {
        final boolean isInit = isInitDone();
        if (needInit != isInit) {
            throw new InitException(isInit);
        }
    }

    boolean isInitDone() {
        synchronized (initLock) {
            return configuration != null;
        }
    }

    void restartRegisterOnBoot() {
        OPFLog.methodD();

        checkInit(true);
        settings.clear();
        register();
    }

    void register(@NonNull final String providerName) {
        register(getProviderWithException(providerName));
    }

    void unregister(@NonNull final String providerName) {
        unregister(getProviderWithException(providerName));
    }

    void onNeedRetryRegister() {
        OPFLog.methodD(currentProvider);
        OPFLog.d("Current provider : " + currentProvider);
        settings.clear();
        if (currentProvider != null) {
            currentProvider.onRegistrationInvalid();
        }
        register();
    }

    void onProviderUnavailable(@NonNull final PushProvider provider) {
        OPFLog.methodD(provider);

        provider.onUnavailable();
        if (provider.equals(currentProvider)) {
            OPFLog.d("Unavailable provider is equals current provider");

            currentProvider = null;
            settings.clear();

            register(); //Restart registration
        }
    }

    void registerNextAvailableProvider(@Nullable final String prevProviderName) {
        synchronized (registrationLock) {
            OPFLog.methodD(prevProviderName);

            final int providersCount = sortedProvidersList.size();
            final int prevProviderPosition = getProviderPosition(sortedProvidersList, prevProviderName);

            for (int i = (prevProviderPosition + 1) % providersCount, j = 0;
                 j < providersCount;
                 i = (i + 1) % providersCount, j++) {

                final PushProvider provider = sortedProvidersList.get(i);
                final String providerName = provider.getName();
                OPFLog.d("Provider name : " + providerName);
                if (provider.isAvailable() && !registerProviderErrors.containsKey(providerName)) {
                    OPFLog.d("Provider is available.");
                    retryManager.cancelRetryAllOperations(provider.getName());
                    register(provider);
                    return;
                }
            }

            settings.saveState(UNREGISTERED);
            OPFLog.w("No more available providers.");
            eventListenerWrapper.onNoAvailableProvider(appContext, registerProviderErrors);
        }
    }

    void registerPackageChangeReceiver() {
        OPFLog.methodD();
        OPFLog.d("currentProvider == " + currentProvider);
        if (currentProvider != null && packageReceiver == null) {
            packageReceiver = ReceiverUtils
                    .registerPackageChangeReceiver(appContext, currentProvider);
        }
    }

    private void restoreLastProvider() {
        OPFLog.methodD();

        final PushProvider lastProvider = getLastProvider();
        if (lastProvider == null) {
            OPFLog.d("No last provider.");
            return;
        }

        OPFLog.d("Try restore last provider '%s'.", lastProvider);

        if (lastProvider.isAvailable() && lastProvider.isRegistered()) {
            OPFLog.d("Last provider is available and registered");
            currentProvider = lastProvider;
            settings.saveState(REGISTERED);
        } else {
            OPFLog.d("Last provider is unavailable or unregistered");
            settings.clear();

            onProviderUnavailable(lastProvider);
        }
    }

    /**
     * Register first available provider.
     */
    private void registerFirstAvailableProvider() {
        OPFLog.methodD();
        registerNextAvailableProvider(null);
    }

    private int getProviderPosition(@NonNull final List<PushProvider> providers,
                                    @Nullable final String providerName) {
        OPFLog.methodD(providers, providerName);

        final int providersCount = providers.size();
        if (providerName != null) {
            for (int i = 0; i < providersCount; i++) {
                if (providerName.equals(providers.get(i).getName())) {
                    return i;
                }
            }
        }

        return providersCount - 1;
    }

    /**
     * Start register provider.
     *
     * @param provider Provider for registration.
     */
    private void register(@NonNull final PushProvider provider) {
        OPFLog.methodD(provider);

        final String regId = provider.getRegistrationId();
        if (!provider.isRegistered() || TextUtils.isEmpty(regId)) {
            RegisteringTimeoutController.setTimeout(appContext, provider.getName());
            settings.saveState(REGISTERING);
            provider.register();
        } else {
            receivedMessageHandler.onRegistered(provider.getName(), provider.getRegistrationId());
        }
    }

    private void unregister(@NonNull final PushProvider provider) {
        OPFLog.methodD(provider);

        if (provider.isRegistered()) {
            provider.unregister();
        } else {
            receivedMessageHandler.onUnregistered(provider.getName(), provider.getRegistrationId());
        }
    }

    private void unregisterPackageChangeReceiver() {
        OPFLog.methodD();

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
        OPFLog.methodD(providerName);

        for (PushProvider provider : sortedProvidersList) {
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }

        OPFLog.d("There isn't provider with name \"" + providerName + "\"");
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
        OPFLog.methodD(providerName);

        final PushProvider provider = getProvider(providerName);
        if (provider == null) {
            throw new OPFPushException("Provider with name '%s' not found.", providerName);
        }
        return provider;
    }

    @Nullable
    private PushProvider getLastProvider() {
        OPFLog.methodD();

        final String storedProviderName = settings.getLastProviderName();
        if (!TextUtils.isEmpty(storedProviderName)) {
            final PushProvider provider = getProvider(storedProviderName);
            if (provider != null) {
                return provider;
            }

            settings.saveLastProvider(null);
        }

        OPFLog.d("There isn't a stored provider");
        return null;
    }

    private void initSortedProviderList() {
        OPFLog.methodD();
        sortedProvidersList = new ArrayList<>(configuration.getProviders());
        if (!configuration.isSelectSystemPreferred()) {
            OPFLog.d("No system preferred");
            return;
        }

        Collections.sort(sortedProvidersList, new Comparator<PushProvider>() {
            @Override
            public int compare(PushProvider leftProvider, PushProvider rightProvider) {
                final String leftHostAppPackage = leftProvider.getHostAppPackage();
                final String rightHostAppPackage = rightProvider.getHostAppPackage();

                boolean isLeftProviderSystem = false;
                boolean isRightProviderSystem = false;
                if (leftHostAppPackage != null) {
                    isLeftProviderSystem = OPFUtils.isSystemApp(appContext, leftHostAppPackage);
                }
                if (rightHostAppPackage != null) {
                    isRightProviderSystem = OPFUtils.isSystemApp(appContext, rightHostAppPackage);
                }

                if (isLeftProviderSystem == isRightProviderSystem) {
                    return 0;
                } else if (isLeftProviderSystem) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    private void fakeOnUnregistered(@NonNull final String providerName,
                                    @Nullable final String registrationId) {
        OPFLog.methodD(providerName, registrationId);
        currentProvider = null;
        settings.clear();
        registerProviderErrors.clear();
        unregisterPackageChangeReceiver();
        eventListenerWrapper.onUnregistered(appContext, providerName, registrationId);
    }

    /**
     * Handles messages that were received by push provider receiver.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final class ReceivedMessageHandler {

        ReceivedMessageHandler() {
        }

        /**
         * A push provider calls this method when new message is received.
         *
         * @param providerName Name of provider which has received the message.
         * @param extras       Message extras.
         */
        public void onMessage(@NonNull final String providerName,
                              @Nullable final Bundle extras) {
            OPFLog.methodD(providerName);
            if (currentProvider != null && providerName.equals(currentProvider.getName())) {
                settings.saveState(REGISTERED);
                eventListenerWrapper.onMessage(appContext, providerName, extras);
            } else {
                OPFLog.w("Ignore onDeletedMessages from provider " + providerName
                        + ". Current provider is " + currentProvider);
            }
        }

        /**
         * A push provider calls this method when new message is deleted.
         *
         * @param providerName  Name of provider which has received the message.
         * @param messagesCount Deleted messages count. If messages count is unknown, than
         *                      {@link org.onepf.opfpush.OPFConstants#MESSAGES_COUNT_UNKNOWN} value
         *                      is passed as argument.
         */
        public void onDeletedMessages(@NonNull final String providerName,
                                      final int messagesCount) {
            OPFLog.methodD(providerName, messagesCount);
            if (currentProvider != null && providerName.equals(currentProvider.getName())) {
                settings.saveState(REGISTERED);
                eventListenerWrapper.onDeletedMessages(appContext, providerName, messagesCount);
            } else {
                OPFLog.w("Ignore onDeletedMessages from provider " + providerName
                        + ". Current provider is " + currentProvider);
            }
        }

        /**
         * A push provider calls this method when message about successful registration is received.
         *
         * @param providerName   Name of provider which has been registered for pushes.
         * @param registrationId The new registration ID for the instance of your app.
         */
        public void onRegistered(@NonNull final String providerName,
                                 @NonNull final String registrationId) {
            synchronized (registrationLock) {
                OPFLog.methodD(providerName, registrationId);
                OPFLog.i("Successfully register provider '%s'.", providerName);
                retryManager.cancelRetryAllOperations(providerName);

                settings.saveState(REGISTERED);
                settings.saveLastAndroidId(
                        Secure.getString(appContext.getContentResolver(), ANDROID_ID)
                );
                currentProvider = getProviderWithException(providerName);
                settings.saveLastProvider(currentProvider);
                registerProviderErrors.clear();
                registerPackageChangeReceiver();

                eventListenerWrapper.onRegistered(appContext, providerName, registrationId);
            }
        }

        /**
         * A push provider calls this method when message about successful unregistration is received.
         *
         * @param providerName      Name of provider which has been unregistered for pushes.
         * @param oldRegistrationId The registration ID for the instance of your app that is now unregistered.
         */
        public void onUnregistered(@NonNull final String providerName,
                                   @Nullable final String oldRegistrationId) {
            synchronized (registrationLock) {
                OPFLog.methodD(providerName, oldRegistrationId);
                OPFLog.i("Successfully unregister provider '%s'.", providerName);
                retryManager.cancelRetryAllOperations(providerName);
            }
        }

        /**
         * A push providers calls this method when registration error is received.
         *
         * @param providerName Name of provider which has received registration error.
         * @param error        Instance of occurred error.
         */
        public void onRegistrationError(@NonNull final String providerName,
                                        @NonNull final OPFError error) {
            synchronized (registrationLock) {
                OPFLog.methodD(providerName, error);
                retryManager.cancelRetryUnregister(providerName);

                if (isRegistered()) {
                    OPFLog.d("Registration state is REGISTERED");
                    return;
                }

                settings.saveState(UNREGISTERED);
                if (error == SERVICE_NOT_AVAILABLE
                        && retryManager.hasTries(providerName, REGISTER)) {
                    retryManager.postRetryRegister(providerName);
                } else {
                    registerProviderErrors.put(providerName, error);
                    retryManager.reset(providerName, REGISTER);
                    registerNextAvailableProvider(providerName);
                }
            }
        }

        /**
         * A push provider calls this method when unregistration error is received.
         *
         * @param providerName Name of provider which has received unregistration error.
         * @param error        Instance of occurred error.
         */
        public void onUnregistrationError(@NonNull final String providerName,
                                          @NonNull final OPFError error) {
            synchronized (registrationLock) {
                OPFLog.methodD(providerName, error);
                retryManager.cancelRetryRegister(providerName);

                final PushProvider provider = getProviderWithException(providerName);
                if (!provider.isRegistered()) {
                    OPFLog.i("Provider already unregistered.");
                    return;
                }

                if (error == SERVICE_NOT_AVAILABLE
                        && retryManager.hasTries(providerName, UNREGISTER)) {
                    OPFLog.w("Error while unregister provider %1$s : %2$s. Unregistration will be retried",
                            providerName, error);
                    retryManager.postRetryUnregister(providerName);
                } else {
                    OPFLog.e("Error while unregister provider %1$s : %2$s",
                            providerName, error);
                }
            }
        }

        /**
         * A push provider calls this method when error of unknown operation is received.
         * Right callback method is chosen depends on current {@code OPFPushHelper} state.
         *
         * @param providerName Name of provider which has received the error.
         * @param error        Instance of occurred error.
         */
        public void onError(@NonNull final String providerName, @NonNull final OPFError error) {
            synchronized (registrationLock) {
                OPFLog.methodD(providerName, error);

                final State state = settings.getState();
                final boolean isRegistered = getProviderWithException(providerName).isRegistered();
                OPFLog.d("Registration state : " + state + " Provider.isRegistered == " + isRegistered);
                if (state == REGISTERING || !isRegistered) {
                    onRegistrationError(providerName, error);
                } else {
                    onUnregistrationError(providerName, error);
                }
            }
        }
    }
}
