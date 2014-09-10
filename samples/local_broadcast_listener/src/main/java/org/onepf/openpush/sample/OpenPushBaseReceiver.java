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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.OpenPushConstants;

/**
 * Created by krozov on 07.09.14.
 */
public class OpenPushBaseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (LocalBroadcastListener.ACTION_MESSAGE.equals(action)) {
            onMessage(intent.getStringExtra(LocalBroadcastListener.EXTRA_PROVIDER_NAME),
                    intent.getExtras());
        } else if (LocalBroadcastListener.ACTION_NO_AVAILABLE_PROVIDER.equals(action)) {
            onNoAvailableProvider();
        } else {
            final String providerName = intent.getStringExtra(LocalBroadcastListener.EXTRA_PROVIDER_NAME);
            if (LocalBroadcastListener.ACTION_REGISTERED.equals(action)) {
                onRegistered(providerName,
                        intent.getStringExtra(LocalBroadcastListener.EXTRA_REGISTRATION_ID));
            } else if (LocalBroadcastListener.ACTION_UNREGISTERED.equals(action)) {
                onUnregistered(providerName,
                        intent.getStringExtra(LocalBroadcastListener.EXTRA_REGISTRATION_ID));
            } else if (LocalBroadcastListener.ACTION_REGISTRATION_ERROR.equals(action)) {
                onRegistrationError(providerName,
                        intent.getIntExtra(LocalBroadcastListener.EXTRA_ERROR_ID, OpenPushConstants.NO_ERROR));
            } else if (LocalBroadcastListener.ACTION_UNREGISTRATION_ERROR.equals(action)) {
                onUnregistrationError(providerName,
                        intent.getIntExtra(LocalBroadcastListener.EXTRA_ERROR_ID, OpenPushConstants.NO_ERROR));
            } else if (LocalBroadcastListener.ACTION_DELETED_MESSAGES.equals(action)) {
                Bundle extras = new Bundle(intent.getExtras());
                extras.remove(LocalBroadcastListener.EXTRA_PROVIDER_NAME);
                onDeletedMessages(providerName, extras);
            }
        }
    }

    protected void onMessage(@NotNull String providerName, @Nullable Bundle extras) {
    }

    protected void onDeletedMessages(@NotNull String providerName, @Nullable Bundle extras) {
    }

    protected void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
    }

    protected void onRegistrationError(@NotNull String providerName,
                                       @MagicConstant(intValues = {
                                               OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                               OpenPushConstants.ERROR_INVALID_SENDER,
                                               OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                               OpenPushConstants.ERROR_UNKNOWN,
                                               OpenPushConstants.NO_ERROR,
                                               OpenPushConstants.ERROR_AUTHEFICATION_FAILED})
                                       int errorId) {
    }

    protected void onUnregistrationError(@NotNull String providerName,
                                       @MagicConstant(intValues = {
                                               OpenPushConstants.ERROR_INVALID_PARAMETERS,
                                               OpenPushConstants.ERROR_INVALID_SENDER,
                                               OpenPushConstants.ERROR_SERVICE_NOT_AVAILABLE,
                                               OpenPushConstants.ERROR_UNKNOWN,
                                               OpenPushConstants.NO_ERROR,
                                               OpenPushConstants.ERROR_AUTHEFICATION_FAILED})
                                       int errorId) {
    }

    protected void onNoAvailableProvider() {
    }

    protected void onUnregistered(@NotNull String providerName, @Nullable String oldRegistrationId) {
    }
}
