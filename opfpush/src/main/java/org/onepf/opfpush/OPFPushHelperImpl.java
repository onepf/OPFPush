/*
 * Copyright 2012-2015 One Platform Foundation
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfpush.backoff.InfinityExponentialBackoffManager;
import org.onepf.opfpush.backoff.RetryManager;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.AvailabilityResult;
import org.onepf.opfpush.model.Message;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.model.State;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfpush.notification.NotificationMaker;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;
import org.onepf.opfutils.exception.InitException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.provider.Settings.Secure;
import static android.provider.Settings.Secure.ANDROID_ID;
import static org.onepf.opfpush.OPFConstants.ACTION_NO_AVAILABLE_PROVIDER;
import static org.onepf.opfpush.OPFConstants.ACTION_RECEIVE;
import static org.onepf.opfpush.OPFConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.OPFConstants.ACTION_UNREGISTRATION;
import static org.onepf.opfpush.model.Operation.REGISTER;
import static org.onepf.opfpush.model.Operation.UNREGISTER;
import static org.onepf.opfpush.model.State.REGISTERED;
import static org.onepf.opfpush.model.State.REGISTERING;
import static org.onepf.opfpush.model.State.UNREGISTERED;
import static org.onepf.opfpush.model.UnrecoverablePushError.Type.AVAILABILITY_ERROR;

/**
 * The helper class to manage push providers.
 * <p/>
 * Use {@link #register()} to start a registration and {@link #unregister()} for start an unregistration.
 * The registration and unregistration operations are asynchronous. You can handle results of these operations
 * via the implementation of the {@link org.onepf.opfpush.listener.EventListener} interface or the extension of
 * the {@link org.onepf.opfpush.receiver.OPFPushReceiver} class.
 * <p/>
 * You must initialize the {@link org.onepf.opfpush.OPFPush} class before start using the {@code OPFPushHelper}.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 04.09.2014
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
final class OPFPushHelperImpl extends OPFPushHelper {

    @SuppressWarnings("NullableProblems")
    @NonNull
    private EventListener eventListenerWrapper;

    @NonNull
    private final Context appContext;

    @Nullable
    private PushProvider currentProvider;

    @SuppressWarnings("NullableProblems")
    @NonNull
    private List<PushProvider> sortedProvidersList;

    @NonNull
    private final Map<String, UnrecoverablePushError> registerProviderErrors = new HashMap<>();

    @NonNull
    private final Map<String, String> providersByHostApps = new HashMap<>();

    private volatile Configuration configuration;

    @NonNull
    private final ReceivedMessageHandler receivedMessageHandler = new ReceivedMessageHandlerImpl();

    @NonNull
    private final Settings settings;

    @SuppressWarnings("NullableProblems")
    @NonNull
    private RetryManager retryManager;

    @NonNull
    private final Object registrationLock = new Object();

    @NonNull
    private final Object initLock = new Object();

    OPFPushHelperImpl(@NonNull final Context context) {
        super();
        appContext = context.getApplicationContext();
        settings = Settings.getInstance(context);
    }

    /**
     * If the {@code OPFPushHelper} is unregistered, it chooses a push provider with
     * the highest priority and starts the registration. Does nothing in another case.
     * The registration result can be handled via the implementation of the {@link org.onepf.opfpush.listener.EventListener}
     * interface or the extension of the {@link org.onepf.opfpush.receiver.OPFPushReceiver} class.
     * <p/>
     * The priority of providers corresponds to the order in which they was added to the
     * {@link org.onepf.opfpush.configuration.Configuration} before the initialization.
     * If you set {@code true} as an argument of {@link org.onepf.opfpush.configuration.Configuration.Builder#setSelectSystemPreferred(boolean)}
     * method, the system push provider will get the highest priority.
     *
     * @throws java.lang.IllegalStateException If {@code OPFPush} haven't been initialized.
     */
    @Override
    public void register() {
        OPFLog.logMethod();

        checkInit(true);
        synchronized (registrationLock) {
            final State state = settings.getState();
            OPFLog.d("Register state : " + state.toString());
            switch (state) {
                case REGISTERED:
                case REGISTERING:
                    break;
                case UNREGISTERED:
                    registerFirstAvailableProvider();
                    break;
            }
        }
    }

    /**
     * If the {@code OPFPushHelper} is registered or registration is in process,
     * starts the asynchronous unregistration of the current push provider. Does nothing
     * if the {@code OPFPushHelper} has already been unregistered.
     * <p/>
     * You should rarely (if ever) need to call this method. Not only is it expensive in terms of resources,
     * but it invalidates your registration ID, which you should never change unnecessarily.
     * A better approach is to simply have your server stop sending messages.
     * Only use unregister if you want to change your sender ID.
     *
     * @throws java.lang.IllegalStateException If {@code OPFPush} haven't been initialized.
     */
    @Override
    public void unregister() {
        OPFLog.logMethod();

        checkInit(true);
        synchronized (registrationLock) {
            final State state = settings.getState();
            OPFLog.d("Registration state = " + state.toString());
            if (currentProvider == null) {
                OPFLog.w("Current provider null");
                return;
            }
            final String providerName = currentProvider.getName();
            final boolean isCurrentProviderRegistered = currentProvider.isRegistered();

            if (state == REGISTERING || state == REGISTERED || isCurrentProviderRegistered) {
                retryManager.cancelRetryUnregister(providerName);

                final String oldRegistrationId = currentProvider.getRegistrationId();
                unregister(currentProvider.getName());
                fakeOnUnregistered(providerName, oldRegistrationId);
            } else {
                OPFLog.w("Unregistration wasn't performed because already unregistered.");
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration", "PMD.UnusedPrivateMethod"})
    private boolean canSendMessages() {
        throw new UnsupportedOperationException("Not supported in current version");
    }

    @SuppressWarnings({"UnusedDeclaration", "PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
    private void sendMessage(@NonNull final Message message) {
        throw new UnsupportedOperationException("Not supported in current version");
    }

    /**
     * Returns the registration ID if there's the registered push provider, null otherwise.
     *
     * @return The registration ID if there's the registered push provider, null otherwise.
     */
    @Nullable
    @Override
    public String getRegistrationId() {
        if (currentProvider != null) {
            return currentProvider.getRegistrationId();
        }

        return null;
    }

    /**
     * Returns the current provider name if there's the registered push provider, null otherwise.
     *
     * @return The current provider name if there's the registered push provider, null otherwise.
     */
    @Nullable
    @Override
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
    @Override
    public PushProvider getCurrentProvider() {
        return currentProvider;
    }

    /**
     * Returns the {@link org.onepf.opfpush.OPFPushHelper.ReceivedMessageHandler} instance.
     * Intended for the internal use, should never be called directly.
     *
     * @return The {@link org.onepf.opfpush.OPFPushHelper.ReceivedMessageHandler} instance.
     */
    @NonNull
    @Override
    public ReceivedMessageHandler getReceivedMessageHandler() {
        checkInit(true);
        return receivedMessageHandler;
    }

    /**
     * Returns {@code true} if the {@code OPFPushHelper} is registered, false otherwise.
     *
     * @return {@code true} if the {@code OPFPushHelper} is registered, false otherwise.
     */
    @Override
    public boolean isRegistered() {
        return settings.getState() == REGISTERED;
    }

    /**
     * Returns {@code true} if the registration operation is being performed at the moment.
     *
     * @return {@code true} if the registration operation is being performed at the moment.
     */
    @Override
    public boolean isRegistering() {
        return settings.getState() == REGISTERING;
    }

    @NonNull
    @Override
    public String toString() {
        return "OPFPushHelper{"
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
    @Override
    void init(@NonNull final Configuration initialConfiguration) {
        OPFLog.logMethod(initialConfiguration);

        if (this.configuration == null) {
            synchronized (initLock) {
                if (this.configuration == null) {
                    this.configuration = initialConfiguration;
                }
            }
        }

        final List<PushProvider> providers = configuration.getProviders();
        for (PushProvider provider : providers) {
            provider.checkManifest(configuration.getCheckManifestHandler());
            providersByHostApps.put(provider.getHostAppPackage(), provider.getName());
        }

        retryManager = RetryManager.init(appContext, InfinityExponentialBackoffManager.getInstance());

        final EventListener eventListener = configuration.getEventListener();
        final boolean isOPFReceiverRegistered = isOPFReceiverRegistered();

        OPFLog.d("isOPFReceiverRegistered == " + isOPFReceiverRegistered
                + "; eventListenerWrapper == " + eventListener);
        if (isOPFReceiverRegistered && eventListener != null) {
            throw new IllegalStateException("You can't register OPFReceiver and set event listener");
        } else if (!isOPFReceiverRegistered && eventListener == null) {
            throw new IllegalStateException("You must register OPFReceiver or set event listener");
        }

        initSortedProviderList();
        this.eventListenerWrapper = EventListenerWrapperCreator
                .getEventListenerWrapper(eventListener);
        restoreLastProvider();
        OPFLog.i("Init done.");
    }

    @Override
    boolean isInitDone() {
        synchronized (initLock) {
            return configuration != null;
        }
    }

    @Override
    void restartRegisterOnBoot() {
        OPFLog.logMethod();

        checkInit(true);
        settings.clear();
        register();
    }

    @Override
    void register(@NonNull final String providerName) {
        synchronized (registrationLock) {
            register(getProviderWithException(providerName));
        }
    }

    @Override
    void unregister(@NonNull final String providerName) {
        synchronized (registrationLock) {
            unregister(getProviderWithException(providerName));
        }
    }

    @Override
    void onNeedRetryRegister() {
        OPFLog.logMethod();
        OPFLog.d("Current provider : " + currentProvider);
        settings.clear();
        if (currentProvider != null) {
            currentProvider.onRegistrationInvalid();
            currentProvider = null;
        }
        register();
    }

    @SuppressWarnings("PMD.OneDeclarationPerLine")
    @Override
    void registerNextAvailableProvider(@Nullable final String prevProviderName) {
        synchronized (registrationLock) {
            OPFLog.logMethod(prevProviderName);

            final int providersCount = sortedProvidersList.size();
            final int prevProviderPosition = getProviderPosition(sortedProvidersList, prevProviderName);

            for (int i = (prevProviderPosition + 1) % providersCount, j = 0;
                 j < providersCount;
                 i = (i + 1) % providersCount, j++) {

                final PushProvider provider = sortedProvidersList.get(i);
                final String providerName = provider.getName();
                OPFLog.d("Provider name : " + providerName);

                final AvailabilityResult providerAvailability = provider.getAvailabilityResult();
                if (!providerAvailability.isAvailable()) {
                    final Integer availabilityErrorCode = providerAvailability.getErrorCode();
                    OPFLog.d("Provider isn't available. Error code : " + availabilityErrorCode);
                    if (availabilityErrorCode != null) {
                        registerProviderErrors.put(
                                providerName,
                                new UnrecoverablePushError(
                                        AVAILABILITY_ERROR,
                                        providerName,
                                        availabilityErrorCode
                                )
                        );
                    }
                } else if (!registerProviderErrors.containsKey(providerName)) {
                    OPFLog.d("Provider is available.");
                    retryManager.cancelRetryRegister(providerName);
                    register(provider);
                    return;
                }
            }

            settings.saveState(UNREGISTERED);
            OPFLog.w("No more available providers.");
            eventListenerWrapper.onNoAvailableProvider(appContext, registerProviderErrors);
        }
    }

    @Nullable
    @Override
    String getProviderNameByHostApp(@Nullable final String appPackage) {
        OPFLog.logMethod(appPackage);
        return providersByHostApps.get(appPackage);
    }

    private void cancelAllOperationsForProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        retryManager.cancelRetryAllOperations(providerName);
    }

    private void checkInit(final boolean needInit) {
        final boolean isInit = isInitDone();
        if (needInit != isInit) {
            throw new InitException(isInit);
        }
    }

    private void onProviderUnavailable(@NonNull final PushProvider provider) {
        OPFLog.logMethod(provider);
        final String providerName = provider.getName();

        cancelAllOperationsForProvider(providerName);
        settings.removeRegisteringProvider(providerName);
        settings.removeUnregisteringProvider(providerName);
        provider.onUnavailable();
    }

    private void restoreLastProvider() {
        OPFLog.logMethod();

        final PushProvider lastProvider = getLastProvider();
        if (lastProvider == null) {
            OPFLog.i("No last provider.");
            return;
        }

        OPFLog.d("Try restore last provider '%s'.", lastProvider);

        if (lastProvider.getAvailabilityResult().isAvailable() && lastProvider.isRegistered()) {
            OPFLog.i("Last provider is available and registered");
            currentProvider = lastProvider;
            settings.saveState(REGISTERED);
        } else {
            OPFLog.i("Last provider is unavailable or unregistered");
            settings.clear();
            currentProvider = null;
            onProviderUnavailable(lastProvider);
        }
    }

    /**
     * Registers a first available provider.
     */
    private void registerFirstAvailableProvider() {
        OPFLog.logMethod();
        registerNextAvailableProvider(null);
    }

    private int getProviderPosition(@NonNull final List<PushProvider> providers,
                                    @Nullable final String providerName) {
        OPFLog.logMethod(providers, providerName);

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
     * Starts register the provider.
     *
     * @param provider Provider for registration.
     */
    private void register(@NonNull final PushProvider provider) {
        OPFLog.logMethod(provider);

        final String regId = provider.getRegistrationId();
        final String providerName = provider.getName();
        if (settings.isProviderUnregistrationPerforming(providerName)) {
            OPFLog.i("Unregistration is being performed for provider %s", provider);

            //reset retrying backoff
            retryManager.cancelRetryUnregister(providerName);
            unregister(providerName);

            settings.savePendingRegistrationProvider(providerName);
        } else if (provider.isRegistered() && !TextUtils.isEmpty(regId) && !isRegistering()) {
            OPFLog.i("Provider %s as already registered", provider);

            settings.removePendingRegistrationProvider();
            receivedMessageHandler.onRegistered(provider.getName(), regId);
        } else if (provider.getAvailabilityResult().isAvailable()) {
            OPFLog.i("Provider %s is available", provider);

            settings.saveState(REGISTERING);
            settings.removePendingRegistrationProvider();
            settings.saveRegisteringProvider(providerName);
            RegisteringTimeoutController.setTimeout(appContext, providerName);
            provider.register();
        } else {
            OPFLog.i("Provider %s is no available", provider);

            settings.removePendingRegistrationProvider();
            onProviderUnavailable(provider);
            register();
        }
    }

    private void unregister(@NonNull final PushProvider provider) {
        OPFLog.logMethod(provider);

        final String providerName = provider.getName();
        if (settings.isProviderRegistrationPerforming(providerName)) {
            OPFLog.i("Registration is being performed for provider %s", provider);

            //reset retrying backoff
            retryManager.cancelRetryRegister(providerName);
            register(providerName);

            settings.savePendingUnregistrationProvider(providerName);
        } else if (!provider.isRegistered()) {
            OPFLog.i("Provider %s as already unregistered", provider);

            settings.removePendingUnregistrationProvider();
            receivedMessageHandler.onUnregistered(providerName, provider.getRegistrationId());
        } else if (provider.getAvailabilityResult().isAvailable()) {
            OPFLog.i("Provider %s is available", provider);

            settings.removePendingUnregistrationProvider();
            settings.saveUnregisteringProvider(providerName);
            provider.unregister();
        } else {
            OPFLog.i("Provider %s is no available", provider);

            final String pendingRegistrationProvider = settings.getPendingRegistrationProvider();
            settings.removePendingUnregistrationProvider();
            onProviderUnavailable(provider);
            if (providerName.equals(pendingRegistrationProvider)) {
                registerNextAvailableProvider(providerName);
            }
        }
    }

    @Nullable
    private PushProvider getProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);

        for (PushProvider provider : sortedProvidersList) {
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }

        OPFLog.w("There isn't provider with name \"" + providerName + "\"");
        return null;
    }

    @NonNull
    private PushProvider getProviderWithException(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);

        final PushProvider provider = getProvider(providerName);
        if (provider == null) {
            throw new IllegalStateException(String.format(
                    Locale.US,
                    "Provider with name '%s' not found.",
                    providerName
            ));
        }
        return provider;
    }

    @Nullable
    private PushProvider getLastProvider() {
        OPFLog.logMethod();

        final String storedProviderName = settings.getLastProviderName();
        if (!TextUtils.isEmpty(storedProviderName)) {
            final PushProvider provider = getProvider(storedProviderName);
            if (provider != null) {
                return provider;
            }

            settings.saveLastProvider(null);
        }

        OPFLog.i("There isn't a stored provider");
        return null;
    }

    private void initSortedProviderList() {
        OPFLog.logMethod();
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
        OPFLog.logMethod(providerName, registrationId);
        currentProvider = null;
        settings.saveState(UNREGISTERED);
        settings.saveLastProvider(null);
        registerProviderErrors.clear();
        eventListenerWrapper.onUnregistered(appContext, providerName, registrationId);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private boolean isOPFReceiverRegistered() {
        OPFLog.logMethod();
        final Intent intentBroadcastReceive = new Intent(ACTION_RECEIVE);
        final Intent intentBroadcastRegistration = new Intent(ACTION_REGISTRATION);
        final Intent intentBroadcastUnregistration = new Intent(ACTION_UNREGISTRATION);
        final Intent intentBroadcastNoAvailableProvider = new Intent(ACTION_NO_AVAILABLE_PROVIDER);

        try {
            OPFChecks.checkReceiver(appContext, intentBroadcastReceive);
            OPFChecks.checkReceiver(appContext, intentBroadcastRegistration);
            OPFChecks.checkReceiver(appContext, intentBroadcastUnregistration);
            OPFChecks.checkReceiver(appContext, intentBroadcastNoAvailableProvider);
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    /**
     * Handles messages that were received by a push provider receiver.
     */
    @SuppressWarnings("UnusedDeclaration")
    private final class ReceivedMessageHandlerImpl implements ReceivedMessageHandler {

        private final Handler handler = new Handler(Looper.getMainLooper());
        /**
         * A push provider calls this method when a new message is received.
         *
         * @param providerName The name of the provider which has received the message.
         * @param extras       The message extras.
         */
        @Override
        public void onMessage(@NonNull final String providerName,
                              @Nullable final Bundle extras) {
            OPFLog.logMethod(providerName);
            if (currentProvider != null && providerName.equals(currentProvider.getName())) {
                settings.saveState(REGISTERED);

                //noinspection InnerClassTooDeeplyNested
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //All operations with NotificationMaker should are performed in the main thread.
                        //It saves users from having to make thread safe NotificationMaker.
                        final NotificationMaker notificationMaker = currentProvider.getNotificationMaker();
                        if (extras != null && notificationMaker.needShowNotification(extras)) {
                            notificationMaker.showNotification(appContext, extras);
                        } else {
                            eventListenerWrapper.onMessage(appContext, providerName, extras);
                        }
                    }
                });
            } else {
                OPFLog.w("Ignore onMessage from provider " + providerName
                        + ". Current provider is " + currentProvider);
            }
        }

        /**
         * A push provider calls this method when the server have deleted some pending messages
         * because they were collapsible.
         *
         * @param providerName  The name of the provider which has received the message.
         * @param messagesCount Deleted messages count. If the messages count is unknown, than
         *                      the {@link org.onepf.opfpush.OPFConstants#MESSAGES_COUNT_UNKNOWN} value
         *                      is passed as an argument.
         */
        @Override
        public void onDeletedMessages(@NonNull final String providerName,
                                      final int messagesCount) {
            OPFLog.logMethod(providerName, messagesCount);
            if (currentProvider != null && providerName.equals(currentProvider.getName())) {
                settings.saveState(REGISTERED);
                eventListenerWrapper.onDeletedMessages(appContext, providerName, messagesCount);
            } else {
                OPFLog.w("Ignore onDeletedMessages from provider " + providerName
                        + ". Current provider is " + currentProvider);
            }
        }

        /**
         * A push provider calls this method when the message about successful registration is received.
         *
         * @param providerName   The name of the provider which has been registered for pushes.
         * @param registrationId The new registration ID for the instance of your app.
         */
        @Override
        public void onRegistered(@NonNull final String providerName,
                                 @NonNull final String registrationId) {
            synchronized (registrationLock) {
                OPFLog.logMethod(providerName, registrationId);
                cancelAllOperationsForProvider(providerName);
                settings.removeRegisteringProvider(providerName);

                if (isRegistered()) {
                    OPFLog.d("Registration state is REGISTERED");
                    return;
                }

                OPFLog.i("Successfully register provider '%s'.", providerName);
                settings.saveState(REGISTERED);
                settings.saveLastAndroidId(
                        Secure.getString(appContext.getContentResolver(), ANDROID_ID)
                );
                currentProvider = getProviderWithException(providerName);
                settings.saveLastProvider(currentProvider);
                registerProviderErrors.clear();

                eventListenerWrapper.onRegistered(appContext, providerName, registrationId);

                if (providerName.equals(settings.getPendingUnregistrationProvider())) {
                    unregister(providerName);
                }
            }
        }

        /**
         * A push provider calls this method when the message about successful unregistration is received.
         *
         * @param providerName      The name of the provider which has been unregistered for pushes.
         * @param oldRegistrationId The registration ID for the instance of your app that is now unregistered.
         */
        @Override
        public void onUnregistered(@NonNull final String providerName,
                                   @Nullable final String oldRegistrationId) {
            synchronized (registrationLock) {
                OPFLog.logMethod(providerName, oldRegistrationId);
                OPFLog.i("Successfully unregister provider '%s'.", providerName);
                settings.removeUnregisteringProvider(providerName);
                if (providerName.equals(settings.getPendingRegistrationProvider())) {
                    register(providerName);
                }
            }
        }

        /**
         * A push providers calls this method when a registration error is received.
         *
         * @param providerName The name of the provider which has received the registration error.
         * @param error        The instance of the occurred error.
         */
        @Override
        public void onRegistrationError(@NonNull final String providerName,
                                        @NonNull final PushError error) {
            synchronized (registrationLock) {
                OPFLog.logMethod(providerName, error);

                if (isRegistered()) {
                    OPFLog.d("Registration state is REGISTERED");
                    return;
                }

                OPFLog.i("Registration error has been received : " + error);
                settings.saveState(UNREGISTERED);
                if (error.isRecoverable()
                        && retryManager.hasTries(providerName, REGISTER)) {
                    retryManager.postRetryRegister(providerName);
                } else {
                    registerProviderErrors.put(providerName, (UnrecoverablePushError) error);
                    retryManager.reset(providerName, REGISTER);
                    settings.removeRegisteringProvider(providerName);
                    if (providerName.equals(settings.getPendingUnregistrationProvider())) {
                        unregister(providerName);
                    }

                    registerNextAvailableProvider(providerName);
                }
            }
        }

        /**
         * A push provider calls this method when an unregistration error is received.
         *
         * @param providerName The name of the provider which has received the unregistration error.
         * @param error        The instance of the occurred error.
         */
        @Override
        public void onUnregistrationError(@NonNull final String providerName,
                                          @NonNull final PushError error) {
            synchronized (registrationLock) {
                OPFLog.logMethod(providerName, error);

                final PushProvider provider = getProviderWithException(providerName);
                if (!provider.isRegistered()) {
                    OPFLog.i("Provider already unregistered.");
                    return;
                }

                if (error.isRecoverable()
                        && retryManager.hasTries(providerName, UNREGISTER)) {
                    OPFLog.i("Error while unregister provider %1$s : %2$s. Unregistration will be retried",
                            providerName, error);
                    retryManager.postRetryUnregister(providerName);
                } else {
                    OPFLog.w("Error while unregister provider %1$s : %2$s",
                            providerName, error);
                    settings.removeUnregisteringProvider(providerName);
                    if (providerName.equals(settings.getPendingRegistrationProvider())) {
                        register(providerName);
                    }
                }
            }
        }

        /**
         * A push provider calls this method when an error of unknown operation is received.
         * The right callback method is chosen depends on the current {@code OPFPushHelper} state.
         *
         * @param providerName The name of the provider which has received the error.
         * @param error        The instance of the occurred error.
         */
        @Override
        public void onError(@NonNull final String providerName, @NonNull final PushError error) {
            synchronized (registrationLock) {
                OPFLog.logMethod(providerName, error);

                final State state = settings.getState();
                final boolean isProviderRegistered = getProviderWithException(providerName).isRegistered();
                OPFLog.i("Error occurred. Registration state : "
                        + state + " Provider.isRegistered == " + isProviderRegistered);
                if (state == REGISTERING || !isProviderRegistered) {
                    onRegistrationError(providerName, error);
                } else {
                    onUnregistrationError(providerName, error);
                }
            }
        }
    }
}
