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
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import junit.framework.Assert;

import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static android.provider.Settings.Secure.ANDROID_ID;
import static org.onepf.opfpush.OPFPushLog.LOGD;
import static org.onepf.opfpush.OPFPushLog.LOGI;
import static org.onepf.opfpush.OPFPushLog.LOGW;

/**
 * Main class for manage push providers.
 * For get instance of this class call {@link #getInstance(android.content.Context)}.
 * <p/>
 * Before do any operations with {@code OpenPushHelper} you must call {@link #init(Options)}.
 * <p/>
 * For start select provider for registerWithNext call {@link #register()}.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 04.09.2014
 */
public final class OPFPushHelper {

    /**
     * Use for {@code messagesCount} argument in
     * {@link ReceivedMessageHandler#onDeletedMessages(String, int)} when messages count is unknown.
     */
    public static final int MESSAGES_COUNT_UNKNOWN = Integer.MIN_VALUE;

    static final int STATE_UNREGISTERED = 0;
    static final int STATE_REGISTERING = 1;
    static final int STATE_REGISTERED = 2;
    static final int STATE_UNREGISTERING = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {
            STATE_UNREGISTERED,
            STATE_UNREGISTERING,
            STATE_REGISTERING,
            STATE_REGISTERED
    })
    @interface State {
    }

    @Nullable
    private static OPFPushHelper instance;

    @NonNull
    private EventListenerWrapper eventListener;

    @NonNull
    private final Context appContext;

    @Nullable
    private BroadcastReceiver packageReceiver;

    @Nullable
    private PushProvider currentProvider;

    @Nullable
    private AlarmManager alarmManager;

    private volatile Options options;

    private final Object registrationLock = new Object();
    private final Object initLock = new Object();

    private final ReceivedMessageHandler receivedMessageHandler = new ReceivedMessageHandler();
    private final Settings settings;

    private OPFPushHelper(@NonNull Context context) {
        appContext = context.getApplicationContext();
        settings = new Settings(context);
    }

    /**
     * Get instance of {@code OpenPushHelper}.
     *
     * @param context The current context.
     * @return Instance of {@link OPFPushHelper}.
     */
    @SuppressFBWarnings({"DC_DOUBLECHECK"})
    public static OPFPushHelper getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (OPFPushHelper.class) {
                if (instance == null) {
                    instance = new OPFPushHelper(context);
                }
            }
        }
        return instance;
    }

    static OPFPushHelper newInstance(@NonNull Context context) {
        synchronized (OPFPushHelper.class) {
            instance = new OPFPushHelper(context);
        }
        return instance;
    }

    public ReceivedMessageHandler getReceivedMessageHandler() {
        checkInitDone();
        return receivedMessageHandler;
    }

    /**
     * Is init done and you may work with {@code OpenPushHelper}.
     *
     * @return True if init is done, else - false.
     */
    public boolean isInitDone() {
        synchronized (initLock) {
            return options != null;
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
    public void sendMessage(@NonNull Message message) {
        synchronized (registrationLock) {
            if (currentProvider instanceof SenderPushProvider) {
                ((SenderPushProvider) currentProvider).send(message);
            } else if (isRegistered()) {
                throw new OPFPushException(
                        "Current provider '%s' not support send messages.", currentProvider);
            } else {
                throw new OPFPushException("Provider not registered.");
            }
        }
    }

    /**
     * Is push registered.
     *
     * @return True if push registered, else - false.
     */
    public boolean isRegistered() {
        final int state = settings.getState();
        return state == STATE_REGISTERED || state == STATE_UNREGISTERING;
    }

    public boolean isUnregistered() {
        return settings.getState() == STATE_UNREGISTERED;
    }

    public boolean isRegistering() {
        return settings.getState() == STATE_REGISTERING;
    }

    public boolean isUnregistering() {
        return settings.getState() == STATE_UNREGISTERING;
    }

    private void checkInitDone() {
        if (!isInitDone()) {
            throw new OPFPushException("Before work with OpenPushHelper call init() first.");
        }
    }

    /**
     * Init {@code OpenPushHelper}. You must call this method before do any operation.
     *
     * @param initialOptions Instance of {@code Options}.
     */
    @SuppressFBWarnings({"DC_DOUBLECHECK", "DC_DOUBLECHECK"})
    public void init(@NonNull Options initialOptions) {
        if (isInitDone()) {
            throw new OPFPushException("You can init OpenPushHelper only one time.");
        }

        if (this.options == null) {
            synchronized (initLock) {
                if (this.options == null) {
                    this.options = initialOptions;
                }
            }
        }

        this.eventListener = new EventListenerWrapper(options.getEventListener());
        initLastProvider();
        LOGI("Init done.");
    }

    private void initLastProvider() {
        synchronized (registrationLock) {
            final PushProvider lastProvider = getLastProvider();
            if (lastProvider == null) {
                LOGI("No last provider.");
                return;
            }

            LOGI("Try restore last provider '%s'.", lastProvider);

            if (lastProvider.isAvailable()) {
                if (lastProvider.isRegistered()) {
                    LOGI("Last provider running.");
                    currentProvider = lastProvider;
                    settings.saveState(STATE_REGISTERED);
                } else {
                    LOGI("Last provider need register.");
                    if (!register(lastProvider)) {
                        settings.saveLastProvider(null);
                    }
                }
            } else {
                LOGI("Last provider is unavailable");
                settings.saveLastProvider(null);
                settings.saveState(STATE_UNREGISTERED);

                onProviderUnavailable(lastProvider);
            }
        }
    }

    /**
     * Check if at least one provider available.
     */
    public boolean hasAvailableProvider() {
        for (PushProvider provider : options.getProviders()) {
            if (provider.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    void restartRegisterOnBoot() {
        checkInitDone();
        settings.clear();
        register();
    }


    /**
     * Start select push provider and registered it.
     * <p/>
     * If you want to modify current registered provider, you must call unregister() first.
     * <p/>
     *
     * @throws OPFPushException When try call this method while init not done,
     *                          unregistration process in progress or already registered.
     */
    public void register() {
        checkInitDone();

        synchronized (registrationLock) {
            switch (settings.getState()) {
                case STATE_REGISTERING:
                    break;

                case STATE_UNREGISTERED:
                    settings.saveState(STATE_REGISTERING);
                    if (options.isSelectSystemPreferred()
                            && registerSystemPreferredProvider()) {
                        return;
                    }
                    registerNextProvider(null);
                    break;

                case STATE_UNREGISTERING:
                    //FIXME: Maybe better throw checked exception?
                    throw new OPFPushException("Can't register while unregistration is running.");

                case STATE_REGISTERED:
                    throw new OPFPushException("Attempt to register twice!");
            }
        }
    }

    private boolean registerSystemPreferredProvider() {
        for (PushProvider provider : options.getProviders()) {
            String hostAppPackage = provider.getHostAppPackage();
            if (hostAppPackage != null) {
                if (PackageUtils.isSystemApp(appContext, hostAppPackage)
                        && register(provider)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Register first available provider. Iterate all provider from the next provider after
     * {@code lastProvider} param.
     *
     * @param lastProvider Last provider what check to registerWithNext or null if has no.
     * @return True if find provider that can try to registerWithNext, otherwise false.
     */
    private boolean registerNextProvider(@Nullable PushProvider lastProvider) {
        int nextProviderIndex = 0;
        final List<PushProvider> providers = options.getProviders();
        if (lastProvider != null) {
            int lastProviderIndex = providers.indexOf(lastProvider);
            if (lastProviderIndex != -1) {
                nextProviderIndex = lastProviderIndex + 1;
            }
        }

        for (int providersCount = providers.size();
             nextProviderIndex < providersCount; ++nextProviderIndex) {

            if (register(providers.get(nextProviderIndex))) {
                return true;
            }
        }

        settings.saveState(STATE_UNREGISTERED);
        LOGW("No more available providers.");
        eventListener.onNoAvailableProvider();
        return false;
    }

    boolean register(@NonNull String providerName) {
        return register(getProviderWithException(providerName));
    }

    /**
     * Start register provider.
     *
     * @param provider Provider for registration.
     * @return If provider available and can start registration return true, otherwise - false.
     */
    private boolean register(@NonNull PushProvider provider) {
        if (provider.isAvailable()) {
            if (Utils.isNetworkConnected(appContext)) {
                LOGD("Try register %s.", provider);
                provider.register();
            } else {
                receivedMessageHandler.onRegistrationError(provider.getName(), Error.SERVICE_NOT_AVAILABLE);
            }
            return true;
        }
        return false;
    }

    /**
     * Unregister the application. Calling unregister() stops any messages from the server.
     * This is a not blocking call. You should rarely (if ever) need to call this method.
     * Not only is it expensive in terms of resources, but it invalidates your registration ID,
     * which you should never change unnecessarily.
     * A better approach is to simply have your server stop sending messages.
     * Only use unregister if you want to change your sender ID.
     *
     * @throws OPFPushException When try call this method while init not done,
     *                          registration process in progress or registration not done.
     */
    public void unregister() {
        checkInitDone();

        synchronized (registrationLock) {
            switch (settings.getState()) {
                case STATE_REGISTERED:
                    Assert.assertNotNull(currentProvider);

                    settings.saveState(STATE_UNREGISTERING);
                    unregisterPackageChangeReceiver();
                    currentProvider.unregister();
                    break;

                case STATE_UNREGISTERING:
                    break;

                case STATE_REGISTERING:
                    //FIXME: Maybe better throw checked exception?
                    throw new OPFPushException("Can't unregister when registration in progress.!");

                case STATE_UNREGISTERED:
                    throw new OPFPushException("Before to unregister you must register provider.!");
            }
        }
    }

    private void unregisterPackageChangeReceiver() {
        if (packageReceiver != null) {
            appContext.unregisterReceiver(packageReceiver);
            packageReceiver = null;
        }
    }

    @Nullable
    public PushProvider getCurrentProvider() {
        return currentProvider;
    }

    /**
     * Search provider by name in {@code options} and return in.
     * If {@code} doesn't contain provider with described name return null.
     *
     * @param providerName Name of provider for search.
     * @return Provider with described name or null if nothing have found.
     */
    @Nullable
    private PushProvider getProvider(@NonNull String providerName) {
        for (PushProvider provider : options.getProviders()) {
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }
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
    private PushProvider getProviderWithException(@NonNull String providerName) {
        PushProvider provider = getProvider(providerName);
        if (provider == null) {
            throw new OPFPushException("Provider with name '%s' not found.", providerName);
        }
        return provider;
    }

    @Nullable
    private PushProvider getLastProvider() {
        String storedProviderName = settings.getLastProviderName();
        if (!TextUtils.isEmpty(storedProviderName)) {
            PushProvider provider = getProvider(storedProviderName);
            if (provider != null) {
                return provider;
            }

            settings.saveLastProvider(null);
        }
        return null;
    }

    @Override
    public String toString() {
        return "OpenPushHelper{"
                + "options="
                + options
                + ", currentProvider="
                + currentProvider
                + ", initDone="
                + isInitDone()
                + ", registered="
                + isRegistered()
                + '}';
    }

    void postRetryRegister(@NonNull String providerName) {
        Assert.assertNotNull(options.getBackoff());

        final long when = System.currentTimeMillis() + options.getBackoff().getTryDelay();

        LOGI("Post retry register provider '%s' at %s", providerName,
                SimpleDateFormat.getDateTimeInstance().format(new Date(when)));

        Intent intent = new Intent(appContext, RetryBroadcastReceiver.class);
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

    /**
     * Call this method when device state changed and need retry registration.
     * May be call only when the helper in registered state.
     *
     * @throws OPFPushException When call this method when registration not done
     *                          or {@code providerName} isn't current registered provider.
     */
    void onNeedRetryRegister() {
        Assert.assertNotNull(currentProvider);
        LOGD("onNeedRetryRegister(providerName = %s).", currentProvider);

        settings.clear();
        currentProvider.onRegistrationInvalid();
        settings.saveState(STATE_REGISTERING);
        if (!register(currentProvider)) {
            settings.saveState(STATE_UNREGISTERED);
        }
    }

    /**
     * Call this method when provider become unavailable.
     *
     * @param provider Provider that become unavailable.
     * @throws OPFPushException When call this method when registration not done
     *                          or {@code providerName} isn't current registered provider.
     */
    void onProviderUnavailable(@NonNull PushProvider provider) {
        LOGD("onProviderUnavailable(provider = %s).", provider);

        if (currentProvider != null && provider.equals(currentProvider)) {
            currentProvider = null;
            settings.saveState(STATE_UNREGISTERED);
        }

        provider.onUnavailable();
        eventListener.onProviderBecameUnavailable(provider.getName());

        if (options.isRecoverProvider()) {
            OPFPushHelper.this.register(); //Restart registration
        }
    }

    /**
     * Is used for handle received messages by broadcast receivers of concrete providers.
     */
    public final class ReceivedMessageHandler {

        ReceivedMessageHandler() {
        }

        /**
         * Receiver must call this method when new message received.
         *
         * @param providerName Name of provider from what message received.
         * @param extras       Message extras.
         * @throws OPFPushException When call this method when registration not done
         *                          or {@code providerName} isn't current registered provider.
         */
        public void onMessage(@NonNull final String providerName,
                              @Nullable final Bundle extras) {
            checkProviderWorking(providerName);

            LOGD("onMessage(providerName = %s).", providerName);
            eventListener.onMessage(providerName, extras);
        }

        /**
         * Receiver must call this method when new message deleted.
         *
         * @param providerName  Name of provider from what message deleted.
         * @param messagesCount Deleted messages count. If messages count is unknown pass -1.
         * @throws OPFPushException When call this method when registration not done
         *                          or {@code providerName} isn't current registered provider.
         */
        public void onDeletedMessages(@NonNull final String providerName,
                                      final int messagesCount) {
            checkProviderWorking(providerName);

            LOGD("onDeletedMessages(providerName = %s, messagesCount = %d).", providerName, messagesCount);
            eventListener.onDeletedMessages(providerName, messagesCount);
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
                if (!isRegistering()) {
                    //FIXME: maybe better send checked exception
                    throw new IllegalStateException("Result can't be handle.");
                }

                final Backoff backoff = options.getBackoff();
                if (backoff != null) {
                    backoff.reset();
                }

                LOGI("Successfully register provider '%s'.", providerName);
                LOGI("Register id '%s'.", registrationId);
                settings.saveState(STATE_REGISTERED);
                settings.saveLastAndroidId(ANDROID_ID);

                currentProvider = getProviderWithException(providerName);
                settings.saveLastProvider(currentProvider);

                Assert.assertNotNull(registrationId);
                eventListener.onRegistered(providerName, registrationId);

                packageReceiver =
                        PackageUtils.registerPackageChangeReceiver(appContext, currentProvider);
            }
        }

        /**
         * Receiver must call this method on successful unregistration.
         *
         * @param providerName      Name of provider that was unregistered.
         * @param oldRegistrationId The registration ID for the instance of your app that is now unregistered.
         */
        public void onUnregistered(@NonNull final String providerName,
                                   @Nullable final String oldRegistrationId) {
            synchronized (registrationLock) {
                if (!isUnregistering()) {
                    //FIXME: maybe better send checked exception
                    throw new IllegalStateException("Result can't be handle.");
                }

                LOGI("Successfully unregister provider '%s'.", providerName);
                settings.clear();
                currentProvider = null;
                Assert.assertNotNull(oldRegistrationId);
                eventListener.onUnregistered(providerName, oldRegistrationId);
            }
        }

        /**
         * Receiver must call this method when a registration request fails.
         *
         * @param providerName Name of provider the registration of which caused the error.
         * @param error        Instance of occurred error.
         */
        public void onRegistrationError(@NonNull final String providerName,
                                        @NonNull final Error error) {
            synchronized (registrationLock) {
                if (!isRegistering()) {
                    //FIXME: maybe better send checked exception
                    throw new IllegalStateException("Result can't be handle.");
                }

                Assert.assertNotNull(error);

                LOGI("Error register provider '%s'.", providerName);
                final PushProvider provider = getProviderWithException(providerName);

                final Backoff backoff = options.getBackoff();
                if (error == Error.SERVICE_NOT_AVAILABLE
                        && backoff != null && backoff.hasTries()) {
                    postRetryRegister(providerName);
                } else {
                    if (backoff != null) {
                        backoff.reset();
                    }
                    registerNextProvider(provider);
                }

                eventListener.onRegistrationError(providerName, error);
            }
        }

        /**
         * Receiver must call this method when a unregistration request fails.
         *
         * @param providerName Name of provider the unregistration of which caused the error.
         * @param error        Instance of occurred error.
         */
        public void onUnregistrationError(@NonNull final String providerName,
                                          @NonNull final Error error) {
            synchronized (registrationLock) {
                if (!isUnregistering()) {
                    //FIXME: maybe better send checked exception
                    throw new IllegalStateException("Result can't be handle.");
                }

                settings.saveState(STATE_REGISTERED);
                Assert.assertNotNull(error);

                LOGI("Error unregister provider '%s'.", providerName);
                eventListener.onUnregistrationError(providerName, error);
            }
        }

        /**
         * Receiver must call this method when the error occurred by unknown reason.
         *
         * @param providerName Name of provider the registration or unregistration of which caused the error.
         * @param error        Instance of occurred error.
         */
        public void onError(@NonNull final String providerName, @NonNull final Error error) {
            synchronized (registrationLock) {
                final int state = settings.getState();
                switch (state) {
                    case STATE_REGISTERING:
                        onRegistrationError(providerName, error);
                        return;

                    case STATE_UNREGISTERING:
                        onUnregistrationError(providerName, error);
                        return;

                    default:
                        //FIXME: maybe better send checked exception
                        throw new IllegalStateException("Result can't be handle.");
                }
            }
        }

        private void checkProviderWorking(final String providerName) {
            checkInitDone();

            if (!isRegistered()) {
                throw new OPFPushException("Can't receive message when not registered.");
            }
            if (currentProvider != null
                    && !providerName.equalsIgnoreCase(currentProvider.getName())) {
                throw new OPFPushException("Can't receive message from not registered provider. "
                        + "Current provider '%s', message source ='%s'",
                        currentProvider.getName(), providerName);
            }
        }
    }


    /**
     * Wrapper for execute all method on main thread.
     *
     * @author Kirill Rozov
     * @author Roman Savin
     * @since 24.09.14.
     */
    private static class EventListenerWrapper implements EventListener {
        private static final Handler HANDLER = new Handler(Looper.getMainLooper());
        private final EventListener listener;

        EventListenerWrapper(EventListener listener) {
            this.listener = listener;
        }

        @Override
        public void onMessage(@NonNull final String providerName, @Nullable final Bundle extras) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onMessage(providerName, extras);
                }
            });
        }

        @Override
        public void onDeletedMessages(@NonNull final String providerName, final int messagesCount) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onDeletedMessages(providerName, messagesCount);
                }
            });
        }

        @Override
        public void onRegistered(@NonNull final String providerName,
                                 @NonNull final String registrationId) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRegistered(providerName, registrationId);
                }
            });

        }

        @Override
        public void onRegistrationError(@NonNull final String providerName, @NonNull final Error error) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRegistrationError(providerName, error);
                }
            });

        }

        @Override
        public void onUnregistrationError(@NonNull final String providerName,
                                          @NonNull final Error error) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUnregistrationError(providerName, error);
                }
            });
        }

        @Override
        public void onNoAvailableProvider() {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onNoAvailableProvider();
                }
            });
        }

        @Override
        public void onUnregistered(@NonNull final String providerName,
                                   @NonNull final String registrationId) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUnregistered(providerName, registrationId);
                }
            });
        }

        @Override
        public void onProviderBecameUnavailable(@NonNull final String providerName) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    listener.onProviderBecameUnavailable(providerName);
                }
            });
        }
    }
}
