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

package org.onepf.openpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by krozov on 07.09.14.
 */
public class OpenPushBaseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BroadcastListener.ACTION_MESSAGE.equals(action)) {
            onMessage(intent.getStringExtra(BroadcastListener.EXTRA_PROVIDER_NAME),
                    intent.getExtras());
        } else if (BroadcastListener.ACTION_NO_AVAILABLE_PROVIDER.equals(action)) {
            onNoAvailableProvider();
        } else {
            final String providerName = intent.getStringExtra(BroadcastListener.EXTRA_PROVIDER_NAME);
            if (BroadcastListener.ACTION_REGISTERED.equals(action)) {
                onRegistered(providerName,
                        intent.getStringExtra(BroadcastListener.EXTRA_REGISTRATION_ID));
            } else if (BroadcastListener.ACTION_UNREGISTERED.equals(action)) {
                onUnregistered(providerName,
                        intent.getStringExtra(BroadcastListener.EXTRA_REGISTRATION_ID));
            } else if (BroadcastListener.ACTION_ERROR.equals(action)) {
                onError(providerName,
                        intent.getIntExtra(BroadcastListener.EXTRA_ERROR_ID, -1));
            } else if (BroadcastListener.ACTION_DELETED_MESSAGES.equals(action)) {
                Bundle extras = new Bundle(intent.getExtras());
                extras.remove(BroadcastListener.EXTRA_PROVIDER_NAME);
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

    protected void onError(@NotNull String providerName, int errorId) {
    }

    protected void onNoAvailableProvider() {
    }

    protected void onUnregistered(@NotNull String providerName, @Nullable String oldRegistrationId) {
    }
}
