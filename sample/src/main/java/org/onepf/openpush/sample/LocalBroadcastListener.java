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

package org.onepf.openpush.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.OpenPushConstants;
import org.onepf.openpush.OpenPushListener;

/**
 * Created by krozov on 07.09.14.
 */
public class LocalBroadcastListener implements OpenPushListener {

    public static final String ACTION_REGISTERED = "org.onepf.openpush.registered";
    public static final String ACTION_UNREGISTERED = "org.onepf.openpush.unregistered";
    public static final String ACTION_MESSAGE = "org.onepf.openpush.message";
    public static final String ACTION_DELETED_MESSAGES = "org.onepf.openpush.message_deleted";
    public static final String ACTION_REGISTRATION_ERROR = "org.onepf.openpush.registration_error";
    public static final String ACTION_UNREGISTRATION_ERROR = "org.onepf.openpush.unregistration_error";
    public static final String ACTION_NO_AVAILABLE_PROVIDER = "org.onepf.openpush.no_available_provider";
    public static final String ACTION_HOST_APP_REMOVED = "org.onepf.openpush.host_app_removed";

    public static final String EXTRA_PROVIDER_NAME = "org.onepf.openpush.provider_name";
    public static final String EXTRA_REGISTRATION_ID = "org.onepf.openpush.registration_id";
    public static final String EXTRA_ERROR_ID = "org.onepf.openpush.error_id";
    public static final String EXTRA_MESSAGES_COUNT = "org.onepf.openpush.messages_count";
    public static final String EXTRA_HOST_APP_PACKAGE = "org.onepf.openpush.host_app_package";

    @NotNull
    private final Context mAppContext;

    public LocalBroadcastListener(@NotNull Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
        sendBroadcast(ACTION_MESSAGE, providerName, extras);
    }

    @Override
    public void onDeletedMessages(@NotNull String providerName, int messagesCount) {
        Bundle extras = new Bundle(1);
        extras.putInt(LocalBroadcastListener.EXTRA_MESSAGES_COUNT, messagesCount);
        sendBroadcast(ACTION_DELETED_MESSAGES, providerName, extras);
    }

    @Override
    public void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
        Bundle extras = new Bundle(1);
        extras.putString(EXTRA_REGISTRATION_ID, registrationId);
        sendBroadcast(ACTION_REGISTERED, providerName, extras);
    }

    @Override
    public void onRegistrationError(@NotNull String providerName,
                                    @MagicConstant(intValues = {
                                            OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                            OpenPushConstants.ERROR_INVALID_SENDER,
                                            OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                            OpenPushConstants.ERROR_UNKNOWN,
                                            OpenPushConstants.NO_ERROR,
                                            OpenPushConstants.ERROR_AUTHEFICATION_FAILED})
                                    int errorId) {
        Bundle extras = new Bundle(1);
        extras.putInt(EXTRA_ERROR_ID, errorId);
        sendBroadcast(ACTION_REGISTRATION_ERROR, providerName, extras);
    }

    @Override
    public void onNoAvailableProvider() {
        sendBroadcast(ACTION_NO_AVAILABLE_PROVIDER, null, null);
    }

    @Override
    public void onUnregistered(@NotNull String providerName, @Nullable String registrationId) {
        Bundle extras = new Bundle(1);
        extras.putString(EXTRA_REGISTRATION_ID, registrationId);
        sendBroadcast(ACTION_UNREGISTERED, providerName, extras);
    }

    private void sendBroadcast(@NotNull String action,
                               @Nullable String providerName,
                               @Nullable Bundle extras) {
        Intent newIntent = new Intent(action);
        if (extras != null) {
            newIntent.putExtras(extras);
        }
        if (providerName != null) {
            newIntent.putExtra(EXTRA_PROVIDER_NAME, providerName);
        }
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(newIntent);
    }

    @Override
    public void onUnregistrationError(@NotNull String providerName,
                                      @MagicConstant(intValues = {
                                              OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                              OpenPushConstants.ERROR_INVALID_SENDER,
                                              OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                              OpenPushConstants.ERROR_UNKNOWN,
                                              OpenPushConstants.NO_ERROR,
                                              OpenPushConstants.ERROR_AUTHEFICATION_FAILED})
                                      int errorId) {
        Bundle extras = new Bundle(1);
        extras.putInt(EXTRA_ERROR_ID, errorId);
        sendBroadcast(ACTION_UNREGISTRATION_ERROR, providerName, extras);
    }

    @Override
    public void onHostAppRemoved(@NotNull String providerName, @NotNull String hostAppPackage) {
        Bundle extras = new Bundle(1);
        extras.putString(EXTRA_HOST_APP_PACKAGE, hostAppPackage);
        sendBroadcast(ACTION_HOST_APP_REMOVED, providerName, extras);
    }
}
