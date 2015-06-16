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

package org.onepf.opfpush.gcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.google.android.gms.iid.InstanceID;
import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfpush.listener.CheckManifestHandler;
import org.onepf.opfpush.model.AvailabilityResult;
import org.onepf.opfpush.model.Message;
import org.onepf.opfpush.pushprovider.SenderPushProvider;
import org.onepf.opfpush.utils.CheckUtils;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;
import org.onepf.opfutils.exception.WrongThreadException;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.gcm.GoogleCloudMessaging.INSTANCE_ID_SCOPE;
import static org.onepf.opfpush.gcm.GCMConstants.ACTION_REGISTRATION_CALLBACK;
import static org.onepf.opfpush.gcm.GCMConstants.ACTION_UNREGISTRATION_CALLBACK;
import static org.onepf.opfpush.gcm.GCMConstants.C2DM_ACTION_RECEIVE;
import static org.onepf.opfpush.gcm.GCMConstants.GOOGLE_CLOUD_MESSAGING_CLASS_NAME;
import static org.onepf.opfpush.gcm.GCMConstants.GOOGLE_PLAY_APP_PACKAGE;
import static org.onepf.opfpush.gcm.GCMConstants.GOOGLE_SERVICES_FRAMEWORK_PACKAGE;
import static org.onepf.opfpush.gcm.GCMConstants.MESSAGES_TO_SUFFIX;
import static org.onepf.opfpush.gcm.GCMConstants.PERMISSION_C2D_MESSAGE_SUFFIX;
import static org.onepf.opfpush.gcm.GCMConstants.PERMISSION_RECEIVE;
import static org.onepf.opfpush.gcm.GCMConstants.PERMISSION_SEND;
import static org.onepf.opfpush.gcm.GCMConstants.PROVIDER_NAME;

/**
 * Google Cloud Messaging push provider implementation.
 *
 * @author Kirill Rozov
 * @see <a href="https://developer.android.com/google/gcm/index.html">Google Cloud Messaging for Android</a>
 * @since 04.09.14
 */
@SuppressWarnings("PMD.GodClass")
public class GCMProvider extends BasePushProvider implements SenderPushProvider {

    private final String senderID;

    @Nullable
    private ExecutorService registrationExecutor;

    @NonNull
    private final PreferencesProvider preferencesProvider;

    public GCMProvider(@NonNull final Context context, @NonNull final String senderID) {
        super(context, PROVIDER_NAME, GOOGLE_PLAY_APP_PACKAGE);

        this.senderID = senderID;
        preferencesProvider = PreferencesProvider.getInstance(context);
    }

    @Override
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void register() {
        OPFLog.logMethod();
        OPFLog.i("Start register GCMProvider.");
        executeTask(new RegisterTask());
    }

    @Override
    @SuppressWarnings({"PMD.AvoidSynchronizedAtMethodLevel", "PMD.AccessorClassGeneration"})
    public synchronized void unregister() {
        OPFLog.logMethod();
        OPFLog.i("Start unregister GCMProvider.");
        executeTask(new UnregisterTask(preferencesProvider.getRegistrationId()));
    }

    @Override
    public void checkManifest(@Nullable final CheckManifestHandler checkManifestHandler) {
        OPFLog.logMethod();
        super.checkManifest(checkManifestHandler);
        final Context context = getContext();
        CheckUtils.checkPermission(context, PERMISSION_RECEIVE, checkManifestHandler);

        final String c2dmPermission = context.getPackageName() + PERMISSION_C2D_MESSAGE_SUFFIX;
        CheckUtils.checkPermission(context, c2dmPermission, checkManifestHandler);

        CheckUtils.checkService(context, new ComponentName(context, GCMRegistrationService.class), checkManifestHandler);
        CheckUtils.checkService(context, new ComponentName(context, SendMessageService.class), checkManifestHandler);

        final Intent c2dmReceiveBroadcastIntent = new Intent(C2DM_ACTION_RECEIVE);
        final Intent registrationBroadcastIntent = new Intent(ACTION_REGISTRATION_CALLBACK);
        final Intent unregistrationBroadcastIntent = new Intent(ACTION_UNREGISTRATION_CALLBACK);

        final String gcmReceiverName = GCMRegistrationReceiver.class.getName();

        CheckUtils.checkReceiver(context, gcmReceiverName, c2dmReceiveBroadcastIntent,
                PERMISSION_SEND, checkManifestHandler);
        CheckUtils.checkReceiver(context, gcmReceiverName, registrationBroadcastIntent,
                PERMISSION_SEND, checkManifestHandler);
        CheckUtils.checkReceiver(context, gcmReceiverName, unregistrationBroadcastIntent,
                PERMISSION_SEND, checkManifestHandler);
    }

    @NonNull
    @Override
    public AvailabilityResult getAvailabilityResult() {
        OPFLog.logMethod();
        if (!isReceivePermissionDeclared()) {
            OPFLog.d("com.google.android.c2dm.permission.RECEIVE permission isn't declared");
            return new AvailabilityResult(false);
        }

        //Need verify that GCM classes present, because dependency provided.
        try {
            Class.forName(GOOGLE_CLOUD_MESSAGING_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            return new AvailabilityResult(false);
        }

        if (super.getAvailabilityResult().isAvailable()) {
            final int conResult = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(getContext());
            if (conResult == ConnectionResult.SUCCESS) {
                return new AvailabilityResult(true);
            } else {
                OPFLog.d("Google Play Services not available. Reason: '%s'.",
                        GooglePlayServicesUtil.getErrorString(conResult));
                return new AvailabilityResult(conResult);
            }
        }
        return new AvailabilityResult(false);
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return preferencesProvider.getRegistrationId();
    }

    @Override
    public boolean isRegistered() {
        OPFLog.logMethod();
        return !TextUtils.isEmpty(preferencesProvider.getRegistrationId());
    }

    @Override
    public void onRegistrationInvalid() {
        OPFLog.logMethod();
        preferencesProvider.reset();
    }

    @Override
    public void onUnavailable() {
        OPFLog.logMethod();
        close();
    }

    @Override
    public void send(@NonNull final Message message) {
        OPFLog.logMethod(message);

        if (!isRegistered()) {
            OPFLog.e("Registration state isn't REGISTERED");
            throw new IllegalStateException("Before send message you need register GCM.");
        }

        final Intent intent = new Intent(getContext(), SendMessageService.class);
        intent.putExtra(SendMessageService.EXTRA_MESSAGE, message);
        intent.putExtra(SendMessageService.EXTRA_MESSAGES_TO, senderID + MESSAGES_TO_SUFFIX);
        getContext().startService(intent);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%s (senderId: '%s')", PROVIDER_NAME, senderID);
    }

    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    private synchronized void close() {
        OPFLog.logMethod();

        if (registrationExecutor != null) {
            OPFLog.d("Registration executor is not null");

            registrationExecutor.shutdownNow();
            registrationExecutor = null;
        }

        preferencesProvider.reset();
        GoogleCloudMessaging.getInstance(getContext()).close();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void executeTask(@NonNull final Runnable runnable) {
        OPFLog.logMethod(runnable);

        if (registrationExecutor == null || registrationExecutor.isShutdown()) {
            registrationExecutor = Executors.newSingleThreadExecutor();
        }
        registrationExecutor.execute(runnable);
    }

    private boolean isReceivePermissionDeclared() {
        final int permissionState = getContext().getPackageManager()
                .checkPermission(PERMISSION_RECEIVE, GOOGLE_SERVICES_FRAMEWORK_PACKAGE);
        return permissionState == PERMISSION_GRANTED;
    }

    private final class RegisterTask implements Runnable {

        @Override
        @SuppressWarnings("PMD.PreserveStackTrace")
        public void run() {
            OPFLog.logMethod();

            try {
                final String registrationId =
                        InstanceID.getInstance(getContext()).getToken(senderID, INSTANCE_ID_SCOPE);
                if (TextUtils.isEmpty(registrationId)) {
                    OPFLog.w("Registration id is empty");
                    onAuthError();
                } else {
                    OPFLog.d("Registration id isn't empty");
                    onRegistrationSuccess(registrationId);
                }
            } catch (IOException e) {
                OPFLog.i("Error while register GCM.", e);

                final String error = e.getMessage();
                switch (error) {
                    case GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE:
                        onServicesNotAvailable();
                        break;
                    case GoogleCloudMessaging.ERROR_MAIN_THREAD:
                        throw new WrongThreadException(false);
                    default:
                        onError(error);
                        break;
                }
            }
        }

        private void onServicesNotAvailable() {
            OPFLog.logMethod();

            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onRegistrationSuccess(@NonNull final String registrationId) {
            OPFLog.logMethod(registrationId);
            preferencesProvider.saveRegistrationId(registrationId);

            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_REGISTRATION_ID, registrationId);

            OPFLog.d("Send broadcast intent : " + OPFUtils.toString(intent));
            getContext().sendBroadcast(intent);
        }

        private void onAuthError() {
            OPFLog.logMethod();
            onError(GCMConstants.ERROR_AUTHENTICATION_FAILED);
        }

        private void onError(@NonNull final String errorId) {
            OPFLog.logMethod(errorId);
            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, errorId);
            getContext().sendBroadcast(intent);
        }
    }

    private final class UnregisterTask implements Runnable {

        @Nullable
        private final String oldRegistrationId;

        private UnregisterTask(@Nullable String oldRegistrationToken) {
            oldRegistrationId = oldRegistrationToken;
        }

        @Override
        @SuppressWarnings("PMD.PreserveStackTrace")
        public void run() {
            OPFLog.logMethod();

            try {
                InstanceID.getInstance(getContext()).deleteToken(senderID, INSTANCE_ID_SCOPE);
                close();

                onUnregistrationSuccess();
            } catch (IOException e) {
                OPFLog.i("Error while unregister GCM.", e);

                final String error = e.getMessage();
                switch (error) {
                    case GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE:
                        onServicesNotAvailable();
                        break;
                    case GoogleCloudMessaging.ERROR_MAIN_THREAD:
                        throw new WrongThreadException(false);
                    default:
                        onError(error);
                        break;
                }
            }
        }

        private void onServicesNotAvailable() {
            OPFLog.logMethod();

            final Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onUnregistrationSuccess() {
            OPFLog.logMethod();

            final Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_REGISTRATION_ID, oldRegistrationId);

            OPFLog.d("Send broadcast intent : " + OPFUtils.toString(intent));
            getContext().sendBroadcast(intent);
        }

        private void onError(@NonNull final String errorId) {
            OPFLog.logMethod(errorId);
            final Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, errorId);
            getContext().sendBroadcast(intent);
        }
    }
}
