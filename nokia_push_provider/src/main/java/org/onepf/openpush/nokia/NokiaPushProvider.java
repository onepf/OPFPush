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

import org.onepf.openpush.BasePushProvider;


public class NokiaPushProvider extends BasePushProvider {

    public static final String NAME = "com.nokia.push.provider"; //todo check the name

    private static final String IMPLEMENTATION_CLASS_NAME = "com.nokia.push.PushRegistrar";

    private final Context contextId;
    private final String senderId;

    public NokiaPushProvider(Context context, String senderID) {
        super(IMPLEMENTATION_CLASS_NAME);

        contextId = context.getApplicationContext();
        senderId = senderID;
    }

    @Override
    public boolean available() {
        try {
            PushRegistrar.checkDevice(contextId);
            return true;
        } catch (UnsupportedOperationException exception) {
            return false;
        }
    }

    @Override
    public String getRegistrationId() {
        return PushRegistrar.getRegistrationId(contextId);
    }

    @Override
    public boolean isRegistered() {
        return PushRegistrar.isRegistered(contextId);
    }

    @Override
    public void register() {
        if (!isRegistered()) {
            PushRegistrar.register(contextId, senderId);
        }
    }

    @Override
    public void unregister() {
        PushRegistrar.unregister(contextId);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
