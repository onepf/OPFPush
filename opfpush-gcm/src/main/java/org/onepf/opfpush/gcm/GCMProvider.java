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
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.SenderPushProvider;
import org.onepf.opfpush.model.Message;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.PackageUtils;
import org.onepf.opfpush.util.Utils;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Google Cloud Messaging push provider implementation.
 *
 * @author Kirill Rozov
 * @see <a href="https://developer.android.com/google/gcm/index.html">Google Cloud Messaging for Android</a>
 * @since 04.09.14
 */
public class GCMProvider extends BasePushProvider implements SenderPushProvider {

    public static final String NAME = "Google Cloud Messaging";
    public static final String GOOGLE_PLAY_APP_PACKAGE = "com.android.vending";

    static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    static final String ANDROID_RELEASE_4_0_4 = "4.0.4";

    static final String PERMISSION_RECEIVE = "com.google.android.c2dm.permission.RECEIVE";
    static final String PERMISSION_C2D_MESSAGE_SUFFIX = ".permission.C2D_MESSAGE";
    public static final String GOOGLE_CLOUD_MESSAGING_CLASS_NAME
            = "com.google.android.gms.gcm.GoogleCloudMessaging";
    private static final String MESSAGES_TO_SUFFIX = "@gcm.googleapis.com";

    private final String senderID;

    @Nullable
    private ExecutorService registrationExecutor;

    @NonNull
    private final GCMSettings settings;

    public GCMProvider(@NonNull Context context, @NonNull String senderID) {
        super(context, NAME, GOOGLE_PLAY_APP_PACKAGE);

        this.senderID = senderID;
        settings = new GCMSettings(context);
    }

    public synchronized void register() {
        OPFPushLog.methodD(GCMProvider.class, "register");
        executeTask(new RegisterTask());
    }

    public synchronized void unregister() {
        OPFPushLog.methodD(GCMProvider.class, "unregister");
        executeTask(new UnregisterTask(settings.getRegistrationId()));
    }

    @Override
    public boolean checkManifest() {
        OPFPushLog.methodD(GCMProvider.class, "checkManifest");
        final Context ctx = getContext();
        return super.checkManifest()
                && (!needGoogleAccounts() || checkPermission(getContext(), Manifest.permission.GET_ACCOUNTS))
                && checkPermission(ctx, Manifest.permission.WAKE_LOCK)
                && checkPermission(ctx, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                && checkPermission(ctx, PERMISSION_RECEIVE)
                && checkPermission(ctx, ctx.getPackageName() + PERMISSION_C2D_MESSAGE_SUFFIX);
    }

    private boolean needGoogleAccounts() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                && !Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4);
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return settings.getRegistrationId();
    }

    @Override
    public boolean isAvailable() {
        //Need verify that GCM classes present, because dependency provided.
        try {
            Class.forName(GOOGLE_CLOUD_MESSAGING_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            OPFPushLog.e("Can't find Google Cloud Messaging classes.");
            return false;
        }

        if (super.isAvailable()) {
            final int conResult = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(getContext());
            if (conResult == ConnectionResult.SUCCESS) {
                return !needGoogleAccounts() || checkGoogleAccount();
            } else {
                OPFPushLog.w("Google Play Services not available. Reason: '%s'.",
                        GooglePlayServicesUtil.getErrorString(conResult));
            }
        }
        return false;
    }

    private boolean checkGoogleAccount() {
        OPFPushLog.methodD(GCMProvider.class, "checkGoogleAccount");
        if (needGoogleAccounts()) {
            OPFPushLog.d("Need google account");
            // On device with version of Android less than "4.0.4"
            // we need to ensure that the user has at least one google account.
            final Account[] googleAccounts
                    = AccountManager.get(getContext()).getAccountsByType(GOOGLE_ACCOUNT_TYPE);
            return googleAccounts.length != 0;
        } else {
            OPFPushLog.d("Not need google account");
            return true;
        }
    }

    @Override
    public boolean isRegistered() {
        if (TextUtils.isEmpty(settings.getRegistrationId())) {
            return false;
        } else {
            final int registeredVersion = settings.getAppVersion();
            return registeredVersion != GCMSettings.NO_SAVED_APP_VERSION
                    && registeredVersion == getAppVersion();
        }
    }

    int getAppVersion() {
        try {
            return PackageUtils.getAppVersion(getContext());
        } catch (PackageManager.NameNotFoundException e) {
            throw new OPFPushException("Application not found", e);
        }
    }

    public void close() {
        OPFPushLog.methodD(GCMProvider.class, "close");

        settings.reset();
        if (registrationExecutor != null) {
            OPFPushLog.d("Registration executor is not null");

            registrationExecutor.shutdownNow();
            registrationExecutor = null;
        }

        GoogleCloudMessaging.getInstance(getContext()).close();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%s (senderId: '%s', appVersion: %d)",
                NAME, senderID, settings.getAppVersion());
    }

    @Override
    public void onRegistrationInvalid() {
        OPFPushLog.methodD(GCMProvider.class, "onRegistrationInvalid");
        settings.saveRegistrationId(null);
        settings.removeAppVersion();
    }

    @Override
    public void onUnavailable() {
        OPFPushLog.methodD(GCMProvider.class, "onUnavailable");
        close();
    }

    @Override
    public void send(@NonNull final Message message) {
        OPFPushLog.methodD(GCMProvider.class, "send", message);

        if (!isRegistered()) {
            OPFPushLog.e("Registration state isn't REGISTERED");
            throw new IllegalStateException("Before send message you need register GCM.");
        }

        final Intent intent = new Intent(getContext(), SendMessageService.class);
        intent.putExtra(SendMessageService.EXTRA_MESSAGE, message);
        intent.putExtra(SendMessageService.EXTRA_MESSAGES_TO, senderID + MESSAGES_TO_SUFFIX);
        getContext().startService(intent);
    }

    private void executeTask(final Runnable runnable) {
        OPFPushLog.methodD(GCMProvider.class, "executeTask", runnable);

        if (registrationExecutor == null || registrationExecutor.isShutdown()) {
            registrationExecutor = Executors.newSingleThreadExecutor();
        }
        registrationExecutor.execute(runnable);
    }

    private final class RegisterTask implements Runnable {

        @Override
        public void run() {
            OPFPushLog.methodD(RegisterTask.class, "run");

            try {
                final String registrationId =
                        GoogleCloudMessaging.getInstance(getContext()).register(senderID);
                if (TextUtils.isEmpty(registrationId)) {
                    OPFPushLog.d("Registration id is empty");
                    onAuthError();
                } else {
                    OPFPushLog.d("Registration id isn't empty");
                    onRegistrationSuccess(registrationId);
                }
            } catch (IOException e) {
                OPFPushLog.e(e.getCause().toString());

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
            OPFPushLog.methodD(RegisterTask.class, "onServicesNotAvailable");

            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onRegistrationSuccess(final String registrationId) {
            OPFPushLog.methodD(RegisterTask.class, "onRegistrationSuccess", "registrationId");

            settings.saveRegistrationId(registrationId);
            settings.saveAppVersion(getAppVersion());

            //For finish registration we catch intent with action
            //GCMConstant.ACTION_REGISTRATION in GCMReceiver.
            //No need to send another intent.
        }

        private void onAuthError() {
            OPFPushLog.methodD(RegisterTask.class, "onAuthError");

            final Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                    GCMConstants.ERROR_AUTHENTICATION_FAILED);
            getContext().sendBroadcast(intent);
        }
    }

    private final class UnregisterTask implements Runnable {

        @NonNull
        private final String oldRegistrationId;

        private UnregisterTask(@NonNull String oldRegistrationToken) {
            oldRegistrationId = oldRegistrationToken;
        }

        @Override
        public void run() {
            OPFPushLog.methodD(UnregisterTask.class, "run");

            try {
                GoogleCloudMessaging.getInstance(getContext()).unregister();
                close();

                onUnregistrationSuccess();
            } catch (IOException e) {
                OPFPushLog.e(e.getCause().toString());

                final String error = e.getMessage();
                switch (error) {
                    case GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE:
                        onServicesNotAvailable();
                        break;
                    case GoogleCloudMessaging.ERROR_MAIN_THREAD:
                        throw new OPFPushException("GCM unregister crash.", e);
                    default:
                        throw new OPFPushException("Unknown exception occur.", e);
                }
            }
        }

        private void onServicesNotAvailable() {
            OPFPushLog.methodD(UnregisterTask.class, "onServicesNotAvailable");

            final Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID, GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onUnregistrationSuccess() {
            OPFPushLog.methodD(UnregisterTask.class, "onUnregistrationSuccess");

            final Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_REGISTRATION_ID, oldRegistrationId);

            OPFPushLog.d("Send broadcast intent : " + Utils.toString(intent));
            getContext().sendBroadcast(intent);
        }
    }
}
