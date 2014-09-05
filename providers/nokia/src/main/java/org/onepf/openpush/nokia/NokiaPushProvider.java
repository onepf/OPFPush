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
import android.support.annotation.NonNull;

import com.nokia.push.PushRegistrar;

import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.exception.RegistrationException;


public class NokiaPushProvider extends BasePushProvider {

    public static final String NAME = "com.nokia.push.provider"; //todo check the name
    private static final String NOKIA_STORE_PACKAGE = "com.nokia.store";
    private static final String IMPLEMENTATION_CLASS_NAME = "com.nokia.push.PushRegistrar";

    private final String senderId;

    public NokiaPushProvider(@NonNull Context context, @NonNull String senderID) {
        super(context, IMPLEMENTATION_CLASS_NAME);
        senderId = senderID;
    }

    @Override
    public boolean isAvailable() {
        try {
            PushRegistrar.checkDevice(getContext());
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
    public void register() throws RegistrationException{
        if (isRegistered()) {
            throw new RegistrationException("Nokia Push already registered.");
        } else {
            PushRegistrar.register(getContext(), senderId);
        }
    }

    @Override
    public void unregister() throws RegistrationException{
        if (isRegistered()) {
            PushRegistrar.unregister(getContext());
        } else {
            throw new RegistrationException("Nokia Push must be registered before unregister.");
        }
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @NonNull
    @Override
    public String getHostAppPackage() {
        return NOKIA_STORE_PACKAGE;
    }
}
