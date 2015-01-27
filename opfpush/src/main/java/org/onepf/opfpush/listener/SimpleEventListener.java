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

package org.onepf.opfpush.listener;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.model.OPFError;

/**
 * @author Roman Savin
 * @since 03.12.14
 */
public class SimpleEventListener implements EventListener {

    @Override
    public void onMessage(@NonNull String providerName, @Nullable Bundle extras) {

    }

    @Override
    public void onDeletedMessages(@NonNull String providerName, int messagesCount) {

    }

    @Override
    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {

    }

    @Override
    public void onUnregistered(@NonNull String providerName, @Nullable String registrationId) {

    }

    @Override
    public void onRegistrationError(@NonNull String providerName, @NonNull OPFError error) {

    }

    @Override
    public void onUnregistrationError(@NonNull String providerName, @NonNull OPFError error) {

    }

    @Override
    public void onNoAvailableProvider() {

    }
}
