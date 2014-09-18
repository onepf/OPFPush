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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushException;
import org.onepf.openpush.util.PackageUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import support.AsyncTaskCompat;

import static org.onepf.openpush.OpenPushLog.LOGE;
import static org.onepf.openpush.OpenPushLog.LOGI;

public class GCMProvider extends BasePushProvider {

    public static final String NAME = "com.google.android.gms.gcm.provider";

    private static final String PREF_REGISTRATION_TOKEN = "registration_token";
    private static final String PREF_APP_VERSION = "app_version";
    private static final String PREF_MESSAGE_ID = "message_id";
    static final String PREF_ANDROID_ID = "android_id";

    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String ANDROID_RELEASE_4_0_4 = "4.0.4";
    static final String PREFERENCES_NAME = "org.onepf.openpush.gcm";
    private static final String PERMISSION_RECEIVE = "com.google.android.c2dm.permission.RECEIVE";

    private volatile String mRegistrationToken;
    private final String[] mSenderIDs;
    private final SharedPreferences mPreferences;
    private final GoogleCloudMessaging mGoogleCloudMessaging;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final AtomicInteger mMsgId;

    private AtomicInteger mTryNumber = new AtomicInteger(1);

    public GCMProvider(@NotNull Context context, @NotNull String senderID, String... senderIDs) {
        super(context, NAME, "com.android.vending");
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
        if (isRegistered()) {
            throw new OpenPushException("Google Cloud Messaging already registered.");
        }
        mExecutor.execute(new RegisterTask());
    }

    public void unregister() {
        if (isRegistered()) {
            reset();
            mExecutor.execute(new UnregisterTask(getContext(), mRegistrationToken));
        } else {
            throw new OpenPushException("Google Cloud Messaging must" +
                    " be registered before unregister.");
        }
    }

    @Override
    public boolean checkManifest() {
        Context ctx = getContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                && !Build.VERSION.RELEASE.equals(ANDROID_RELEASE_4_0_4)
                && !checkPermission(ctx, Manifest.permission.GET_ACCOUNTS)) {
            return false;
        }

        return super.checkManifest()
                && checkPermission(ctx, android.Manifest.permission.WAKE_LOCK)
                && checkPermission(ctx, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                && checkPermission(ctx, PERMISSION_RECEIVE)
                && checkPermission(ctx, ctx.getPackageName() + ".permission.C2D_MESSAGE");
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
            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext())
                    == ConnectionResult.SUCCESS) {

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
            } else {
                return false;
            }
        } else {
            return false;
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
    }

    @NotNull
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
    }

    private void reset() {
        mRegistrationToken = null;
        mPreferences.edit().clear().apply();
    }

    public void send(@NotNull GCMMessage msg) {
        send(mSenderIDs[0], msg);
    }

    @NotNull
    public String[] getSenderIDs() {
        return mSenderIDs;
    }

    public void send(@NotNull String senderId, @NotNull GCMMessage msg) {
        final int msgId = mMsgId.incrementAndGet();
        mPreferences.edit()
                .putInt(PREF_MESSAGE_ID, msgId)
                .apply();
        AsyncTaskCompat.execute(new SendMessageTask(getContext(), senderId, msg));
    }

    private static class SendMessageTask extends AsyncTask<Void, Void, Void> {
        private static final String GCM_SENDER_SUFFIX = "@gcm.googleapis.com";

        private final String mTo;
        private final GCMMessage mMessage;
        private final WeakReference<Context> mContextRef;

        private SendMessageTask(@NotNull Context context,
                                @NotNull String senderId,
                                @NotNull GCMMessage message) {
            mTo = senderId + GCM_SENDER_SUFFIX;
            mMessage = message;
            mContextRef = new WeakReference<Context>(context.getApplicationContext());
        }

        protected Void doInBackground(Void... params) {
            Context context = mContextRef.get();
            if (context != null) {
                try {
                    GoogleCloudMessaging.getInstance(context)
                            .send(mTo, mMessage.getId(), mMessage.getTimeToLeave(), mMessage.getData());
                    LOGI(String.format("Message '%s' has sent.", mMessage));
                } catch (IOException ex) {
                    LOGE(String.format("Error while send Message '%s'.", mMessage), ex);
                }
            } else {
                LOGE(String.format("Error while send Message '%s'. No context.", mMessage));
            }
            return null;
        }
    }

    public long getDelay() {
        return TimeUnit.SECONDS.toMillis(2 << (mTryNumber.getAndIncrement() - 1));
    }

    private static class UnregisterTask implements Runnable {
        private final String mOldRegistrationToken;
        private final WeakReference<Context> mContextRef;

        private UnregisterTask(@NotNull Context context, @NotNull String oldRegistrationToken) {
            mOldRegistrationToken = oldRegistrationToken;
            mContextRef = new WeakReference<Context>(context.getApplicationContext());
        }

        @Override
        public void run() {
            final Context context = mContextRef.get();
            if (context != null) {
                try {
                    GoogleCloudMessaging.getInstance(context).unregister();

                    Intent intent = new Intent(GCMConstants.ACTION_UNREGISTRATION);
                    intent.putExtra(GCMConstants.EXTRA_TOKEN, mOldRegistrationToken);
                    context.sendBroadcast(intent);
                } catch (IOException e) {
                    //TODO Send error.
                }
            } else {
                LOGE("Can't unregister - Context is null");
            }
        }
    }

    private class RegisterTask implements Runnable {
        @Override
        public void run() {
            reset();

            try {
                final String registrationId = mGoogleCloudMessaging.register(mSenderIDs);
                if (registrationId != null) {
                    mTryNumber.set(0);
                    mPreferences.edit()
                            .putString(PREF_ANDROID_ID, Settings.Secure.ANDROID_ID)
                            .putString(PREF_REGISTRATION_TOKEN, registrationId)
                            .putInt(PREF_APP_VERSION, PackageUtils.getAppVersion(getContext()))
                            .apply();
                    mRegistrationToken = registrationId;

                    Intent intent = new Intent(GCMConstants.ACTION_REGISTRATION);
                    intent.putExtra(GCMConstants.EXTRA_TOKEN, mRegistrationToken);
                    getContext().sendBroadcast(intent);
                } else {
                    onAuthError();
                }
            } catch (IOException e) {
                if (GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE.equals(e.getMessage())) {
                    long when = System.currentTimeMillis() + getDelay();
                    postRetryRegister(when);

                    Intent intent = new Intent(GCMConstants.ACTION_ERROR);
                    intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                            GCMConstants.ERROR_SERVICE_NOT_AVAILABLE);
                    getContext().sendBroadcast(intent);
                } else {
                   onAuthError();
                }
            }
        }

        private void onAuthError() {
            mTryNumber.set(0);
            Intent intent = new Intent(GCMConstants.ACTION_ERROR);
            intent.putExtra(GCMConstants.EXTRA_ERROR_ID,
                    GCMConstants.ERROR_AUTHEFICATION_FAILED);
            getContext().sendBroadcast(intent);
        }

        private void postRetryRegister(long when) {
            new Handler(Looper.getMainLooper()).postAtTime(new Runnable() {
                @Override
                public void run() {
                    register();
                }
            }, when);
        }
    }
}
