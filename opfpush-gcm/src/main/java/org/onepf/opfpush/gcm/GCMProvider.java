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
import org.onepf.opfpush.SenderPushProvider;
import org.onepf.opfpush.model.Message;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.PackageUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.onepf.opfpush.OPFPushLog.LOGE;
import static org.onepf.opfpush.OPFPushLog.LOGW;

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

    private final String mSenderID;

    @Nullable
    private ExecutorService mRegistrationExecutor;

    @NonNull
    private final Settings mSettings;

    public GCMProvider(@NonNull Context context, @NonNull String senderID) {
        super(context, NAME, GOOGLE_PLAY_APP_PACKAGE);

        mSenderID = senderID;
        mSettings = new Settings(context);
    }

    public synchronized void register() {
        executeTask(new RegisterTask());
    }

    private void executeTask(Runnable runnable) {
        if (mRegistrationExecutor == null || mRegistrationExecutor.isShutdown()) {
            mRegistrationExecutor = Executors.newSingleThreadExecutor();
        }
        mRegistrationExecutor.execute(runnable);
    }

    public synchronized void unregister() {
        executeTask(new UnregisterTask(mSettings.getRegistrationToken()));
    }

    @Override
    public boolean checkManifest() {
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
        return mSettings.getRegistrationToken();
    }

    @Override
    public boolean isAvailable() {
        //Need verify that GCM classes present, because dependency provided.
        try {
            Class.forName(GOOGLE_CLOUD_MESSAGING_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            LOGE("Can't find Google Cloud Messaging classes.");
            return false;
        }

        if (super.isAvailable()) {
            final int conResult =
                    GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
            if (conResult == ConnectionResult.SUCCESS) {
                return !needGoogleAccounts() || checkGoogleAccount();
            } else {
                LOGW("Google Play Services ont available. Reason: '%s'.",
                        GooglePlayServicesUtil.getErrorString(conResult));
            }
        }
        return false;
    }

    private boolean checkGoogleAccount() {
        if (needGoogleAccounts()) {
            // On device with version of Android less than "4.0.4"
            // we need to ensure that the user has at least one google account.
            Account[] googleAccounts
                    = AccountManager.get(getContext()).getAccountsByType(GOOGLE_ACCOUNT_TYPE);
            return googleAccounts.length != 0;
        } else {
            return true;
        }
    }

    @Override
    public boolean isRegistered() {
        if (TextUtils.isEmpty(mSettings.getRegistrationToken())) {
            return false;
        } else {
            final int registeredVersion = mSettings.getAppVersion();
            return registeredVersion != Settings.NO_SAVED_APP_VERSION
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
        mSettings.reset();
        if (mRegistrationExecutor != null) {
            mRegistrationExecutor.shutdownNow();
            mRegistrationExecutor = null;
        }

        GoogleCloudMessaging.getInstance(getContext()).close();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%s (senderId: '%s', appVersion: %d)",
                NAME, mSenderID, mSettings.getAppVersion());
    }

    @Override
    public void onRegistrationInvalid() {
        mSettings.saveRegistrationToken(null);
        mSettings.removeAppVersion();
    }

    @Override
    public void onUnavailable() {
        close();
    }

    @Override
    public void send(@NonNull Message msg) {
        if (!isRegistered()) {
            throw new IllegalStateException("Before send message you need register GCM.");
        }

        Intent intent = new Intent(getContext(), SendMessageService.class);
        intent.putExtra(SendMessageService.EXTRA_MESSAGE, msg);
        intent.putExtra(SendMessageService.EXTRA_MESSAGES_TO, mSenderID + MESSAGES_TO_SUFFIX);
        getContext().startService(intent);
    }

    private final class UnregisterTask implements Runnable {
        private final String mOldRegistrationToken;

        private UnregisterTask(@NonNull String oldRegistrationToken) {
            mOldRegistrationToken = oldRegistrationToken;
        }

        @Override
        public void run() {
            try {
                GoogleCloudMessaging.getInstance(getContext()).unregister();
                close();

                onUnregistrationSuccess();
            } catch (IOException e) {
                if (GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE.equals(e.getMessage())) {
                    onServicesNotAvailable();
                } else if (GoogleCloudMessaging.ERROR_MAIN_THREAD.equals(e.getMessage())) {
                    throw new OPFPushException("GCM unregister crash.", e);
                } else {
                    throw new OPFPushException("Unknown exception occur.", e);
                }
            }
        }

        private void onServicesNotAvailable() {
            Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                    GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onUnregistrationSuccess() {
            Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_REGISTRATION_ID, mOldRegistrationToken);
            getContext().sendBroadcast(intent);
        }
    }

    private final class RegisterTask implements Runnable {
        @Override
        public void run() {
            try {
                final String registrationToken =
                        GoogleCloudMessaging.getInstance(getContext()).register(mSenderID);
                if (registrationToken == null) {
                    onAuthError();
                } else {
                    onRegistrationSuccess(registrationToken);
                }
            } catch (IOException e) {
                String error = e.getMessage();
                if (GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE.equals(error)) {
                    onServicesNotAvailable();
                } else if (GoogleCloudMessaging.ERROR_MAIN_THREAD.equals(error)) {
                    throw new OPFPushException("GCM register crash", e);
                } else {
                    onAuthError();
                }
            }
        }

        private void onServicesNotAvailable() {
            Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                    GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
            getContext().sendBroadcast(intent);
        }

        private void onRegistrationSuccess(final String registrationToken) {
            mSettings.saveRegistrationToken(registrationToken);
            mSettings.saveAppVersion(getAppVersion());

            //For finish registration we catch intent with action
            //GCMConstant.ACTION_REGISTRATION in GCMReceiver.
            //No need to send another intent.
        }

        private void onAuthError() {
            Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION_CALLBACK);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                    GCMConstants.ERROR_AUTHENTICATION_FAILED);
            getContext().sendBroadcast(intent);
        }
    }
}
