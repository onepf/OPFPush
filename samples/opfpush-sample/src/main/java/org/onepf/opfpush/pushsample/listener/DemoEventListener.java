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

package org.onepf.opfpush.pushsample.listener;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.model.State;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class DemoEventListener implements EventListener {

    private static final String TAG = DemoEventListener.class.getSimpleName();

    @Override
    public void onMessage(@NonNull String providerName, @Nullable Bundle extras) {
        Log.d(TAG, String.format("onMessage(%1$s, %2$s)", providerName, extras));
    }

    @Override
    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {
        Log.d(TAG, String.format("onDeletedMessages(%1$s, %2$s)", providerName, messagesCount));
    }

    @Override
    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
        Log.d(TAG, String.format("onRegistered(%1$s, %2$s)", providerName, registrationId));
    }

    @Override
    public void onUnregistered(@NonNull String providerName, @NonNull String registrationId) {
        Log.d(TAG, String.format("onUnregistered(%1$s, %2$s)", providerName, registrationId));
    }

    @Override
    public void onRegistrationError(@NonNull String providerName, @NonNull OPFError error) {
        Log.d(TAG, String.format("onRegistrationError(%1$s, %2$s)", providerName, error));
    }

    @Override
    public void onUnregistrationError(@NonNull String providerName, @NonNull OPFError error) {
        Log.d(TAG, String.format("onUnregistrationError(%1$s, %2$s)", providerName, error));
    }

    @Override
    public void onRegistrationStateError(@NonNull String providerName, @NonNull State state) {
        Log.d(TAG, String.format("onRegistrationStateError(%1$s, %2$s)", providerName, state));
    }

    @Override
    public void onUnregistrationStateError(@NonNull String providerName, @NonNull State state) {
        Log.d(TAG, String.format("onUnregistrationStateError(%1$s, %2$s)", providerName, state));
    }

    @Override
    public void onNoAvailableProvider() {
        Log.d(TAG, "onNoAvailableProvider()");
    }

    @Override
    public void onWrongStateError(@NonNull String providerName, @NonNull OPFError error, @NonNull State state) {
        Log.d(TAG, String.format("onWrongStateError(%1$s, %2$s, %3$s)", providerName, error, state));
    }

    @Override
    public void onProviderBecameUnavailable(@NonNull String providerName) {
        Log.d(TAG, String.format("onProviderBecameUnavailable(%s)", providerName));
    }
}
