/*******************************************************************************
 * Copyright 2014 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

package org.onepf.openpush.nokia;

import android.content.Context;

import com.nokia.push.PushRegistrar;

import org.jetbrains.annotations.NotNull;
import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.exception.OpenPushException;
import org.onepf.openpush.exception.RegistrationException;


public class NokiaPushProvider extends BasePushProvider {

    public static final String NAME = "com.nokia.push.provider"; //todo check the name

    private final String senderId;

    public NokiaPushProvider(@NotNull Context context, @NotNull String senderID) {
        super(context, "com.nokia.push.PushRegistrar");
        senderId = senderID;
    }

    @Override
    public boolean isAvailable() {
        try {
            PushRegistrar.checkDevice(getContext());
            PushRegistrar.checkManifest(getContext());
            return true;
        } catch (UnsupportedOperationException exception) {
            return false;
        }
    }

    @Override
    public String getRegistrationId() {
        return PushRegistrar.getRegistrationId(getContext());
    }

    @Override
    public boolean isRegistered() {
        return PushRegistrar.isRegistered(getContext());
    }

    @Override
    public void register() {
        if (isRegistered()) {
            throw new OpenPushException("Nokia Push already registered.");
        } else {
            PushRegistrar.register(getContext(), senderId);
        }
    }

    @Override
    public void unregister() throws RegistrationException {
        if (isRegistered()) {
            PushRegistrar.unregister(getContext());
        } else {
            throw new RegistrationException("Nokia Push must be registered before unregister.");
        }
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public String getHostAppPackage() {
        return "com.nokia.store";
    }
}
