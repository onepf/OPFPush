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

package org.onepf.pushchat.retrofit;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.google.gson.GsonBuilder;
import org.onepf.opfutils.OPFLog;
import org.onepf.pushchat.PushChatApplication;
import org.onepf.pushchat.controller.StateController;
import org.onepf.pushchat.db.DatabaseHelper;
import org.onepf.pushchat.db.DatabaseHelper.ContactsUuidsAsyncQueryHandler.QueryContactsUuidsCallback;
import org.onepf.pushchat.model.request.RegistrationRequestBody;
import org.onepf.pushchat.model.request.UnregistrationRequestBody;
import org.onepf.pushchat.model.request.push.PushMessageRequestBody;
import org.onepf.pushchat.model.response.ExistResponse;
import org.onepf.pushchat.model.response.RegistrationResponse;
import org.onepf.pushchat.model.response.UnregistrationResponse;
import org.onepf.pushchat.model.response.push.PushMessageResponse;
import org.onepf.pushchat.utils.Constants;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import java.util.Set;

import static org.onepf.pushchat.ui.activity.MainActivity.MainActivityReceiver.HIDE_PROGRESS_BAR_ACTION;
import static org.onepf.pushchat.utils.Constants.PROVIDER_NAME_EXTRA_KEY;
import static org.onepf.pushchat.utils.Constants.REGISTERED_ACTION;
import static org.onepf.pushchat.utils.Constants.UNREGISTERED_ACTION;

/**
 * @author Roman Savin
 * @since 19.04.2015
 */
public final class NetworkController {

    private final PushService pushService;

    @SuppressWarnings("PMD.AccessorClassGeneration")
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

        pushService.register(body, registrationCallback(context, providerName, registrationId));
    }

    public void unregister(@NonNull final Context context) {
        final UnregistrationRequestBody body = new UnregistrationRequestBody(getUuid(context));
        pushService.unregister(body, unregistrationCallback(context));
    }

    public void pushMessage(@NonNull final Context context,
                            @NonNull final String message,
                            @NonNull final Callback<PushMessageResponse> pushMessageCallback) {
        DatabaseHelper.getInstance(context).queryAllContactsUuids(
                new QueryContactsUuidsCallback() {
                    @Override
                    public void onComplete(@NonNull final Set<String> uuids) {
                        final PushMessageRequestBody body = new PushMessageRequestBody(
                                uuids,
                                getUuid(context),
                                message
                        );
                        pushService.push(body, pushMessageCallback);
                    }
                });
    }

    public void exist(@NonNull final String uuid,
                      @NonNull final Callback<ExistResponse> callback) {
        pushService.exist(uuid, callback);
    }

    private String getUuid(@NonNull final Context context) {
        final PushChatApplication application = (PushChatApplication) context.getApplicationContext();
        return application.getUUID();
    }

    private Callback<RegistrationResponse> registrationCallback(
            @NonNull final Context context,
            @NonNull final String providerName,
            @NonNull final String registrationId
    ) {
        return new Callback<RegistrationResponse>() {
            @Override
            public void success(@NonNull final RegistrationResponse registrationResponse,
                                @NonNull final Response response) {
                OPFLog.logMethod(registrationResponse, response);

                StateController.putRegIdSavedOnServerValue(context, true);

                final Intent registeredIntent = new Intent(REGISTERED_ACTION);
                registeredIntent.putExtra(PROVIDER_NAME_EXTRA_KEY, providerName);
                registeredIntent.putExtra(Constants.REGISTRATION_ID_EXTRA_KEY, registrationId);
                context.sendBroadcast(registeredIntent);
                context.sendBroadcast(new Intent(HIDE_PROGRESS_BAR_ACTION));
            }

            @Override
            public void failure(@NonNull final RetrofitError error) {
                OPFLog.logMethod(error);
                OPFLog.e(error.getMessage());
                register(context, providerName, registrationId);
            }
        };
    }

    private Callback<UnregistrationResponse> unregistrationCallback(
            @NonNull final Context context
    ) {
        return new Callback<UnregistrationResponse>() {
            @Override
            public void success(@NonNull final UnregistrationResponse unregistrationResponse,
                                @NonNull final Response response) {
                OPFLog.logMethod(unregistrationResponse, response);

                StateController.putRegIdSavedOnServerValue(context, false);

                context.sendBroadcast(new Intent(UNREGISTERED_ACTION));
                context.sendBroadcast(new Intent(HIDE_PROGRESS_BAR_ACTION));
            }

            @Override
            public void failure(@NonNull final RetrofitError error) {
                OPFLog.logMethod(error);
                OPFLog.e(error.getMessage());
                unregister(context);
            }
        };
    }
}
