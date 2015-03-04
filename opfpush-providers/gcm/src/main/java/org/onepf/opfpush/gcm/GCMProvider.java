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

package org.onepf.opfpush.gcm;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfpush.SenderPushProvider;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.model.Message;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.onepf.opfpush.gcm.GCMConstants.ANDROID_RELEASE_4_0_4;
import static org.onepf.opfpush.gcm.GCMConstants.GOOGLE_ACCOUNT_TYPE;
import static org.onepf.opfpush.gcm.GCMConstants.GOOGLE_CLOUD_MESSAGING_CLASS_NAME;
import static org.onepf.opfpush.gcm.GCMConstants.GOOGLE_PLAY_APP_PACKAGE;
import static org.onepf.opfpush.gcm.GCMConstants.GOOGLE_SERVICES_FRAMEWORK_PACKAGE;
import static org.onepf.opfpush.gcm.GCMConstants.MESSAGES_TO_SUFFIX;
import static org.onepf.opfpush.gcm.GCMConstants.PERMISSION_C2D_MESSAGE_SUFFIX;
import static org.onepf.opfpush.gcm.GCMConstants.PERMISSION_RECEIVE;
import static org.onepf.opfpush.gcm.GCMConstants.PROVIDER_NAME;
import static org.onepf.opfutils.OPFUtils.hasRequestedPermission;

/**
 * Google Cloud Messaging push provider implementation.
 *
 * @author Kirill Rozov
 * @see <a href="https://developer.android.com/google/gcm/index.html">Google Cloud Messaging for Android</a>
 * @since 04.09.14
 */
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
    public synchronized void register() {
        OPFLog.methodD();
        OPFLog.i("Start register GCMProvider.");
        executeTask(new RegisterTask());
    }

    @Override
    public synchronized void unregister() {
        OPFLog.methodD();
        OPFLog.i("Start unregister GCMProvider.");
        executeTask(new UnregisterTask(preferencesProvider.getRegistrationId()));
    }

    @Override
    public boolean checkManifest() {
        OPFLog.methodD();
        final Context context = getContext();
        return super.checkManifest()
                && (!needGoogleAccounts() || hasRequestedPermission(getContext(), Manifest.permission.GET_ACCOUNTS))
                && hasRequestedPermission(context, Manifest.permission.WAKE_LOCK)
                && hasRequestedPermission(context, PERMISSION_RECEIVE)
                && hasRequestedPermission(context, context.getPackageName() + PERMISSION_C2D_MESSAGE_SUFFIX);
    }

    @Override
    public boolean isAvailable() {
        OPFLog.methodD();
        final Context context = getContext();
        if (!isReceivePermissionDeclared()) {
            OPFLog.i("com.google.android.c2dm.permission.RECEIVE permission isn't declared");
            return false;
        }

        //Need verify that GCM classes present, because dependency provided.
        try {
            Class.forName(GOOGLE_CLOUD_MESSAGING_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            OPFLog.e("Can't find Google Cloud Messaging classes.");
            return false;
        }

        if (super.isAvailable()) {
            final int conResult = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(context);
            if (conResult == ConnectionResult.SUCCESS) {
                return !needGoogleAccounts() || checkGoogleAccount();
            } else {
                OPFLog.w("Google Play Services not available. Reason: '%s'.",
                        GooglePlayServicesUtil.getErrorString(conResult));
            }
        }
        return false;
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return preferencesProvider.getRegistrationId();
    }

    @Override
    public boolean isRegistered() {
        OPFLog.methodD();
        return !TextUtils.isEmpty(preferencesProvider.getRegistrationId());
    }

    @Override
    public void onRegistrationInvalid() {
        OPFLog.methodD();
        preferencesProvider.reset();
    }

    @Override
    public void onUnavailable() {
        OPFLog.methodD();
        close();
    }

    @Override
    public void send(@NonNull final Message message) {
        OPFLog.methodD(message);

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

    private boolean needGoogleAccounts() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                && !Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4);
    }

    private boolean checkGoogleAccount() {
        OPFLog.methodD();
        if (needGoogleAccounts()) {
            OPFLog.d("Need google account");
            // On device with version of Android less than "4.0.4"
            // we need to ensure that the user has at least one google account.
            final Account[] googleAccounts
                    = AccountManager.get(getContext()).getAccountsByType(GOOGLE_ACCOUNT_TYPE);
            return googleAccounts.length != 0;
        } else {
            OPFLog.d("Not need google account");
            return true;
        }
    }

    private void close() {
        OPFLog.methodD();

        preferencesProvider.reset();
        if (registrationExecutor != null) {
            OPFLog.d("Registration executor is not null");

            registrationExecutor.shutdownNow();
            registrationExecutor = null;
        }

        GoogleCloudMessaging.getInstance(getContext()).close();
    }

    private void executeTask(@NonNull final Runnable runnable) {
        OPFLog.methodD(runnable);

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
        public void run() {
            OPFLog.methodD();

            try {
                final String registrationId =
                        GoogleCloudMessaging.getInstance(getContext()).register(senderID);
                if (TextUtils.isEmpty(registrationId)) {
                    OPFLog.d("Registration id is empty");
                    onAuthError();
                } else {
                    OPFLog.d("Registration id isn't empty");
                    onRegistrationSuccess(registrationId);
                }
            } catch (IOException e) {
                OPFLog.e("Error while register GCM.", e);

                final String error = e.getMessage();
                switch (error) {
                    case GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE:
                        onServicesNotAvailable();
                        break;
                    case GoogleCloudMessaging.ERROR_MAIN_THREAD:
                        throw new OPFPushException("GCM register crash", e);
                    default:
                        onAuthError();
                        break;
                }
            }
        }

        private void onServicesNotAvailable() {
            OPFLog.methodD();

            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onRegistrationSuccess(@NonNull final String registrationId) {
            OPFLog.methodD(registrationId);
            preferencesProvider.saveRegistrationId(registrationId);

            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_REGISTRATION_ID, registrationId);

            OPFLog.d("Send broadcast intent : " + OPFUtils.toString(intent));
            getContext().sendBroadcast(intent);
        }

        private void onAuthError() {
            OPFLog.methodD();

            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                    GCMConstants.ERROR_AUTHENTICATION_FAILED);
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
        public void run() {
            OPFLog.methodD();

            try {
                GoogleCloudMessaging.getInstance(getContext()).unregister();
                close();

                onUnregistrationSuccess();
            } catch (IOException e) {
                OPFLog.e("Error while unregister GCM.", e);

                final String error = e.getMessage();
                switch (error) {
                    case GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE:
                        onServicesNotAvailable();
                        break;
                    case GoogleCloudMessaging.ERROR_MAIN_THREAD:
                        throw new OPFPushException("GCM unregister crash.", e);
                    default:
                        OPFLog.e("Error while unregister : " + e);
                }
            }
        }

        private void onServicesNotAvailable() {
            OPFLog.methodD();

            final Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onUnregistrationSuccess() {
            OPFLog.methodD();

            final Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_REGISTRATION_ID, oldRegistrationId);

            OPFLog.d("Send broadcast intent : " + OPFUtils.toString(intent));
            getContext().sendBroadcast(intent);
        }
    }
}
