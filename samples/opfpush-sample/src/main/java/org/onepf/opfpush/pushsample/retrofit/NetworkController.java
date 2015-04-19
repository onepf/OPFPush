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

package org.onepf.opfpush.pushsample.retrofit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.GsonBuilder;

import org.onepf.opfpush.pushsample.DemoApplication;
import org.onepf.opfpush.pushsample.model.event.FailedRequestEvent;
import org.onepf.opfpush.pushsample.model.event.RegisteredEvent;
import org.onepf.opfpush.pushsample.model.event.UnregisteredEvent;
import org.onepf.opfpush.pushsample.model.request.push.PushMessageRequestBody;
import org.onepf.opfpush.pushsample.model.request.RegistrationRequestBody;
import org.onepf.opfpush.pushsample.model.request.UnregistrationRequestBody;
import org.onepf.opfpush.pushsample.model.response.push.PushMessageResponse;
import org.onepf.opfpush.pushsample.model.response.RegistrationResponse;
import org.onepf.opfpush.pushsample.model.response.UnregistrationResponse;
import org.onepf.opfutils.OPFLog;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * @author Roman Savin
 * @since 19.04.2015
 */
public final class NetworkController {

    private final PushService pushService;

    private static final class NetworkControllerHolder {
        private static final NetworkController INSTANCE = new NetworkController();
    }

    private NetworkController() {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://onepf-opfpush.appspot.com/_ah/api/opfpush/v1")
                .setConverter(new GsonConverter(new GsonBuilder().create()))
                .build();
        pushService = restAdapter.create(PushService.class);
    }

    public static NetworkController getInstance() {
        return NetworkControllerHolder.INSTANCE;
    }

    public void register(@NonNull final Context context,
                         @NonNull final String providerName,
                         @NonNull final String registrationId) {

        final RegistrationRequestBody body =
                new RegistrationRequestBody(getUuid(context), providerName, registrationId);

        pushService.register(body, registrationCallback(registrationId));
    }

    public void unregister(@NonNull final Context context,
                           @Nullable final String oldRegistrationId) {
        final UnregistrationRequestBody body = new UnregistrationRequestBody(getUuid(context));
        pushService.unregister(body, unregistrationCallback(oldRegistrationId));
    }

    public void pushMessage(@NonNull final Context context,
                            @NonNull final String message) {
        final PushMessageRequestBody body = new PushMessageRequestBody(getUuid(context), message);
        pushService.push(body, pushMessageCallback());
    }

    private String getUuid(@NonNull final Context context) {
        final DemoApplication application = (DemoApplication) context.getApplicationContext();
        return application.getUUID();
    }

    private Callback<RegistrationResponse> registrationCallback(
            @NonNull final String registrationId
    ) {
        return new Callback<RegistrationResponse>() {
            @Override
            public void success(@NonNull final RegistrationResponse registrationResponse,
                                @NonNull final Response response) {
                EventBus.getDefault().postSticky(new RegisteredEvent(registrationId));
            }

            @Override
            public void failure(@NonNull final RetrofitError error) {
                EventBus.getDefault().postSticky(new FailedRequestEvent(error.getMessage()));
            }
        };
    }

    private Callback<UnregistrationResponse> unregistrationCallback(
            @Nullable final String oldRegistrationId
    ) {
        return new Callback<UnregistrationResponse>() {
            @Override
            public void success(@NonNull final UnregistrationResponse unregistrationResponse,
                                @NonNull final Response response) {
                EventBus.getDefault().postSticky(new UnregisteredEvent(oldRegistrationId));
            }

            @Override
            public void failure(@NonNull final RetrofitError error) {
                EventBus.getDefault().postSticky(new FailedRequestEvent(error.getMessage()));
            }
        };
    }

    private Callback<PushMessageResponse> pushMessageCallback() {
        return new Callback<PushMessageResponse>() {
            @Override
            public void success(@NonNull final PushMessageResponse pushMessageResponse,
                                @NonNull final Response response) {
                OPFLog.logMethod(pushMessageResponse, response);
            }

            @Override
            public void failure(@NonNull final RetrofitError error) {
                OPFLog.logMethod(error);
            }
        };
    }
}
