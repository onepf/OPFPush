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

package org.onepf.openpush.gcm;

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

import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushException;
import org.onepf.openpush.PackageUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import support.AsyncTaskCompat;

import static org.onepf.openpush.OpenPushLog.LOGI;

/**
 * Google Cloud Messaging push provider implementation.
 *
 * @author Kirill Rozov
 * @see <a href="https://developer.android.com/google/gcm/index.html">Google Cloud Messaging for Android</a>
 * @since 04.09.14
 */
public class GCMProvider extends BasePushProvider {

    public static final String NAME = "Google Cloud Messaging";
    private static final String GOOGLE_PLAY_APP_PACKAGE = "com.android.vending";

    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String ANDROID_RELEASE_4_0_4 = "4.0.4";

    private static final String PERMISSION_RECEIVE = "com.google.android.c2dm.permission.RECEIVE";
    private static final String PERMISSION_C2D_MESSAGE_SUFFIX = ".permission.C2D_MESSAGE";

    private final String[] mSenderIDs;

    @NonNull
    final GoogleCloudMessaging mGoogleCloudMessaging;

    private final AtomicInteger mMsgId;

    @Nullable
    private ExecutorService mExecutor;

    @NonNull
    final Settings mSettings;

    public GCMProvider(@NonNull Context context, @NonNull String senderID, String... senderIDs) {
        super(context, NAME, GOOGLE_PLAY_APP_PACKAGE);
        mSenderIDs = new String[1 + senderIDs.length];
        mSenderIDs[0] = senderID;
        if (senderIDs.length > 0) {
            System.arraycopy(senderIDs, 0, mSenderIDs, 1, senderIDs.length);
        }

        mSettings = new Settings(context);
        mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(context);
        mMsgId = new AtomicInteger(mSettings.getMessageId());
    }

    public synchronized void register() {
        executeTask(new RegisterTask());
    }

    private void executeTask(Runnable runnable) {
        if (mExecutor == null || mExecutor.isShutdown()) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        mExecutor.execute(runnable);
    }

    public synchronized void unregister() {
        executeTask(new UnregisterTask(mSettings.getRegistrationToken()));
    }

    @Override
    public boolean checkManifest() {
        final Context ctx = getContext();
        return super.checkManifest()
                && !checkGetAccountsPermission()
                && checkPermission(ctx, Manifest.permission.WAKE_LOCK)
                && checkPermission(ctx, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                && checkPermission(ctx, PERMISSION_RECEIVE)
                && checkPermission(ctx, ctx.getPackageName() + PERMISSION_C2D_MESSAGE_SUFFIX);
    }

    private boolean checkGetAccountsPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                && !Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4)
                && !checkPermission(getContext(), Manifest.permission.GET_ACCOUNTS);
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
            Class.forName("com.google.android.gms.gcm.GoogleCloudMessaging");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        if (super.isAvailable()) {
            int conResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
            return conResult == ConnectionResult.SUCCESS && checkGoogleAccount();
        } else {
            return false;
        }
    }

    private boolean checkGoogleAccount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                || Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4)) {
            return true;
        } else {
            // On device with version of Android less than "4.0.4"
            // we need to ensure that the user has at least one google account.
            Account[] googleAccounts
                    = AccountManager.get(getContext()).getAccountsByType(GOOGLE_ACCOUNT_TYPE);
            return googleAccounts.length != 0;
        }
    }

    @Override
    public boolean isRegistered() {
        if (TextUtils.isEmpty(mSettings.getRegistrationToken())) {
            return false;
        } else {
            final int registeredVersion = mSettings.getAppVersion();
            return registeredVersion != Settings.NO_SAVED_APP_VERSION
                    && registeredVersion == PackageUtils.getAppVersion(getContext());
        }
    }

    public void close() {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }

        mGoogleCloudMessaging.close();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s (senderId: '%s', appVersion: %d)", NAME,
                Arrays.toString(mSenderIDs), mSettings.getAppVersion());
    }

    @Override
    public void onRegistrationInvalid() {
        reset();
    }

    @Override
    public void onUnavailable() {
        reset();
        close();
    }

    private void reset() {
        mSettings.reset();
    }

    /**
     * Send message to server.
     *
     * @throws IllegalStateException If try send message when provider isn't registered.
     * @see #send(String, GCMMessage)
     */
    public void send(@NonNull GCMMessage msg) {
        send(mSenderIDs[0], msg);
    }

    @NonNull
    public String[] getSenderIDs() {
        return mSenderIDs;
    }

    /**
     * Send message to server.
     *
     * @throws IllegalStateException If try send message when provider isn't registered.
     * @see #send(GCMMessage)
     */
    public void send(@NonNull String senderId, @NonNull GCMMessage msg) {
        if (!isRegistered()) {
            throw new IllegalStateException("Before send message you need register GCM.");
        }

        final int msgId = mMsgId.incrementAndGet();
        mSettings.saveMessageId(msgId);
        AsyncTaskCompat.execute(new SendMessageTask(getContext(), senderId, msg));
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
                reset();

                onUnregistrationSuccess();
            } catch (IOException e) {
                if (GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE.equals(e.getMessage())) {
                    onServicesNotAvailable();
                } else if (GoogleCloudMessaging.ERROR_MAIN_THREAD.equals(e.getMessage())) {
                    throw new OpenPushException("GCM unregister crash.", e);
                } else {
                    //TODO Notify event about error.
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
                final String registrationToken = mGoogleCloudMessaging.register(mSenderIDs);
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
                    throw new OpenPushException("GCM register crash", e);
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
            mSettings.saveAppVersion(PackageUtils.getAppVersion(getContext()));

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
