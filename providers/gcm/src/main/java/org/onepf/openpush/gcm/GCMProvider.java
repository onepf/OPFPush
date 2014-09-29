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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushException;
import org.onepf.openpush.util.PackageUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    private static final String PREF_REGISTRATION_TOKEN = "registration_token";
    private static final String PREF_APP_VERSION = "app_version";
    private static final String PREF_MESSAGE_ID = "message_id";
    static final String PREF_ANDROID_ID = "android_id";

    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String ANDROID_RELEASE_4_0_4 = "4.0.4";
    static final String PREFERENCES_NAME = "org.onepf.openpush.gcm";
    private static final String PERMISSION_RECEIVE = "com.google.android.c2dm.permission.RECEIVE";
    private static final String GOOGLE_PLAY_APP_PACKAGE = "com.android.vending";
    private static final String PERMISSION_C2D_MESSAGE_SUFFIX = ".permission.C2D_MESSAGE";
    private final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private volatile String mRegistrationToken;
    private final String[] mSenderIDs;
    private final SharedPreferences mPreferences;
    private final GoogleCloudMessaging mGoogleCloudMessaging;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final AtomicInteger mMsgId;

    private AtomicInteger mTryNumber = new AtomicInteger(1);

    public GCMProvider(@NonNull Context context, @NonNull String senderID, String... senderIDs) {
        super(context, NAME, GOOGLE_PLAY_APP_PACKAGE);
        mSenderIDs = new String[1 + senderIDs.length];
        mSenderIDs[0] = senderID;
        if (senderIDs.length > 0) {
            System.arraycopy(senderIDs, 0, mSenderIDs, 1, senderIDs.length);
        }

        mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(context);
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        mRegistrationToken = mPreferences.getString(PREF_REGISTRATION_TOKEN, null);
        mMsgId = new AtomicInteger(mPreferences.getInt(PREF_MESSAGE_ID, 0));
    }

    public void register() {
        if (mExecutor == null || mExecutor.isShutdown()) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        mExecutor.execute(new RegisterTask());
    }

    public void unregister() {
        mExecutor.execute(new UnregisterTask(mRegistrationToken));
    }

    @Override
    public boolean checkManifest() {
        final Context ctx = getContext();
        return super.checkManifest()
                && !checkGetAccountsPermission(ctx)
                && checkPermission(ctx, Manifest.permission.WAKE_LOCK)
                && checkPermission(ctx, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                && checkPermission(ctx, PERMISSION_RECEIVE)
                && checkPermission(ctx, ctx.getPackageName() + PERMISSION_C2D_MESSAGE_SUFFIX);
    }

    private boolean checkGetAccountsPermission(Context ctx) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                && !Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4)
                && !checkPermission(ctx, Manifest.permission.GET_ACCOUNTS);
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return mRegistrationToken;
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
            return conResult == ConnectionResult.SUCCESS && isGoogleAccountExists();
        } else {
            return false;
        }
    }

    private boolean isGoogleAccountExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                || Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4)) {
            return true;
        } else {
            // On device with version of Android less than "4.0.4"
            // we need to ensure that the user has at least one google account.
            Account[] googleAccounts = AccountManager.get(getContext())
                    .getAccountsByType(GOOGLE_ACCOUNT_TYPE);
            return googleAccounts.length != 0;
        }
    }

    @Override
    public boolean isRegistered() {
        if (TextUtils.isEmpty(mRegistrationToken)) {
            return false;
        } else {
            if (mPreferences.contains(PREF_APP_VERSION)) {
                int registeredVersion = mPreferences.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
                return registeredVersion == PackageUtils.getAppVersion(getContext());
            } else {
                return false;
            }
        }
    }

    @Override
    public void close() {
        mMsgId.set(0);
        mGoogleCloudMessaging.close();

        mExecutor.shutdownNow();
        mExecutor = null;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s (senderId: '%s', appVersion: %d)", NAME, Arrays.toString(mSenderIDs),
                mPreferences.getInt(PREF_APP_VERSION, -1));
    }

    @Override
    public void onAppStateChanged() {
        reset();
    }

    @Override
    public void onUnavailable() {
        reset();
        close();
    }

    private void reset() {
        mTryNumber.set(0);
        mRegistrationToken = null;
        mPreferences.edit().clear().apply();
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
        mPreferences.edit()
                .putInt(PREF_MESSAGE_ID, msgId)
                .apply();
        AsyncTaskCompat.execute(new SendMessageTask(getContext(), senderId, msg));
    }

    long getDelay() {
        return TimeUnit.SECONDS.toMillis(2 << (mTryNumber.getAndIncrement() - 1));
    }

    private class UnregisterTask implements Runnable {
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
                    Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION);
                    intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                            GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
                    getContext().sendBroadcast(intent);

                    postUnregisterDelayed();
                } else if (GoogleCloudMessaging.ERROR_MAIN_THREAD.equals(e.getMessage())) {
                    throw new OpenPushException("GCM unregister crash", e);
                } else {
                    //TODO Notify event about error.
                }
            }
        }

        private void postUnregisterDelayed() {
            LOGI("Post unregistration retry.");
            long delay = getDelay();
            MAIN_HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    unregister();
                }
            }, delay);
        }

        private void onUnregistrationSuccess() {
            Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION);
            intent.putExtra(GCMConstants.EXTRA_REGISTRATION_ID, mOldRegistrationToken);
            getContext().sendBroadcast(intent);
        }
    }

    private class RegisterTask implements Runnable {
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
                    Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION);
                    intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                            GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
                    getContext().sendBroadcast(intent);

                    postDelayed();
                } else if (GoogleCloudMessaging.ERROR_MAIN_THREAD.equals(error)) {
                    throw new OpenPushException("GCM register crash", e);
                } else {
                    onAuthError();
                }
            }
        }

        private void onRegistrationSuccess(final String registrationToken) {
            mTryNumber.set(0);
            mRegistrationToken = registrationToken;

            mPreferences.edit()
                    .putString(PREF_ANDROID_ID, Settings.Secure.ANDROID_ID)
                    .putString(PREF_REGISTRATION_TOKEN, registrationToken)
                    .putInt(PREF_APP_VERSION, PackageUtils.getAppVersion(getContext()))
                    .apply();

            //For finish registration we catch intent with action
            //GCMConstant.ACTION_C2DM_REGISTRATION in GCMReceiver.
            //No need to send another intent.
        }

        private void postDelayed() {
            LOGI("Post registration retry.");
            long delay = getDelay();
            MAIN_HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    register();
                }
            }, delay);
        }

        private void onAuthError() {
            mTryNumber.set(0);
            Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                    GCMConstants.ERROR_AUTHENTICATION_FAILED);
            getContext().sendBroadcast(intent);
        }
    }
}
