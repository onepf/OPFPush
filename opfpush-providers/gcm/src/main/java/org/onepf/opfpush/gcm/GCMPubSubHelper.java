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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.gcm.GcmPubSub;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfutils.OPFLog;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.onepf.opfpush.gcm.GCMConstants.GCM_NOT_CURRENT_PROVIDER_ERROR;
import static org.onepf.opfpush.gcm.GCMConstants.PROVIDER_NAME;
import static org.onepf.opfpush.gcm.GCMConstants.REGISTRATION_ID_NOT_OBTAINED_ERROR;

/**
 * The helper class which performs {@link GcmPubSub#subscribe(String, String, Bundle)} and {@link GcmPubSub#unsubscribe(String, String)}
 * in the worker thread using the saved registration id.
 *
 * @author Roman Savin
 * @since 26.06.2015
 */
public final class GCMPubSubHelper {

    private static volatile GCMPubSubHelper instance;

    @NonNull
    private final GcmPubSub gcmPubSub;

    @NonNull
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @NonNull
    private final Object pubSubMonitor = new Object();

    private GCMPubSubHelper(@NonNull final Context context) {
        this.gcmPubSub = GcmPubSub.getInstance(context);
    }

    @SuppressWarnings({"DoubleCheckedLocking", "PMD.NonThreadSafeSingleton"})
    public static GCMPubSubHelper getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (GCMPubSubHelper.class) {
                if (instance == null) {
                    instance = new GCMPubSubHelper(context);
                }
            }
        }

        return instance;
    }

    /**
     * Invokes {@link GcmPubSub#subscribe(String, String, Bundle)} asynchronously.
     * Registration id saved in {@link OPFPushHelper} is used for subscribing.
     *
     * It checks current provider name: if {@link OPFPushHelper#getProviderName()} isn't equal to {@link GCMConstants#PROVIDER_NAME}
     * the {@link org.onepf.opfpush.gcm.GCMPubSubHelper.Callback#onError(String)} method will be called with
     * {@link GCMConstants#GCM_NOT_CURRENT_PROVIDER_ERROR} as the argument.
     *
     * Also it checks if the registration id is already obtained: if {@link OPFPushHelper#getRegistrationId()} returns null
     * the {@link org.onepf.opfpush.gcm.GCMPubSubHelper.Callback#onError(String)} method will be called with
     * {@link GCMConstants#REGISTRATION_ID_NOT_OBTAINED_ERROR} as the argument.
     *
     * @param topic    Developer defined topic name. Must match the following regular expression: "/topics/[a-zA-Z0-9-_.~%]{1,900}".
     * @param extras   An additional information (optional).
     * @param callback The callback instance. Can be null.
     */
    public void subscribe(@NonNull final String topic,
                          @Nullable final Bundle extras,
                          @Nullable final Callback callback) {
        final OPFPushHelper helper = OPFPush.getHelper();
        final Callback callbackWrapper = new CallbackMainThreadWrapper(callback);

        if (checkPubSubAvailability(helper, callbackWrapper)) {
            synchronized (pubSubMonitor) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //noinspection ConstantConditions
                            gcmPubSub.subscribe(helper.getRegistrationId(), topic, extras);
                            callbackWrapper.onSuccess();
                        } catch (final IOException e) {
                            OPFLog.w(e.getMessage());
                            callbackWrapper.onError(e.getMessage());
                        }
                    }
                });
            }
        }
    }

    /**
     * Invokes {@link GcmPubSub#unsubscribe(String, String)} asynchronously.
     * Registration id saved in {@link OPFPushHelper} is used for unsubscribing.
     *
     * It checks current provider name: if {@link OPFPushHelper#getProviderName()} isn't equal to {@link GCMConstants#PROVIDER_NAME}
     * the {@link org.onepf.opfpush.gcm.GCMPubSubHelper.Callback#onError(String)} method will be called with
     * {@link GCMConstants#GCM_NOT_CURRENT_PROVIDER_ERROR} as the argument.
     *
     * Also it checks if the registration id is already obtained: if {@link OPFPushHelper#getRegistrationId()} returns null
     * the {@link org.onepf.opfpush.gcm.GCMPubSubHelper.Callback#onError(String)} method will be called with
     * {@link GCMConstants#REGISTRATION_ID_NOT_OBTAINED_ERROR} as the argument.
     *
     * @param topic    From which to stop receiving messages.
     * @param callback The callback instance. Can be null.
     */
    public void unsubscribe(@NonNull final String topic,
                            @Nullable final Callback callback) {
        final OPFPushHelper helper = OPFPush.getHelper();
        final Callback callbackWrapper = new CallbackMainThreadWrapper(callback);

        if (checkPubSubAvailability(helper, callbackWrapper)) {
            synchronized (pubSubMonitor) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //noinspection ConstantConditions
                            gcmPubSub.unsubscribe(helper.getRegistrationId(), topic);
                            callbackWrapper.onSuccess();
                        } catch (final IOException e) {
                            OPFLog.w(e.getMessage());
                            callbackWrapper.onError(e.getMessage());
                        }
                    }
                });
            }
        }
    }

    private boolean checkPubSubAvailability(@NonNull final OPFPushHelper helper, @NonNull final Callback callback) {
        if (!PROVIDER_NAME.equals(helper.getProviderName())) {
            callback.onError(GCM_NOT_CURRENT_PROVIDER_ERROR);
            return false;
        } else if (helper.getRegistrationId() == null) {
            callback.onError(REGISTRATION_ID_NOT_OBTAINED_ERROR);
            return false;
        }
        return true;
    }

    public interface Callback {

        void onSuccess();

        void onError(@Nullable final String error);
    }

    private class CallbackMainThreadWrapper implements Callback {

        @NonNull
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Nullable
        private final Callback callback;

        public CallbackMainThreadWrapper(@Nullable final Callback callback) {
            this.callback = callback;
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        @Override
        public void onSuccess() {
            if (callback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();
                    }
                });
            }
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        @Override
        public void onError(@Nullable final String error) {
            if (callback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(error);
                    }
                });
            }
        }
    }
}
