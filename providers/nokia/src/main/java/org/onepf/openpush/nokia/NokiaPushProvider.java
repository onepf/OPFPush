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

package org.onepf.openpush.nokia;

import android.content.Context;
import android.support.annotation.NonNull;

import com.nokia.push.PushRegistrar;

import org.onepf.openpush.BasePushProvider;


public class NokiaPushProvider extends BasePushProvider {

    public static final String NAME = "com.nokia.push.provider"; //todo check the name

    private final String[] mSendersId;

    public NokiaPushProvider(@NonNull Context context, @NonNull String... sendersID) {
        super(context, NAME, "com.nokia.store");
        mSendersId = sendersID;
    }

    @Override
    public boolean isAvailable() {
        if (super.isAvailable()) {
            try {
                PushRegistrar.checkDevice(getContext());
                return true;
            } catch (UnsupportedOperationException exception) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean checkManifest() {
        try {
            PushRegistrar.checkManifest(getContext());
            return super.checkManifest();
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
        PushRegistrar.register(getContext(), mSendersId);
    }

    @Override
    public void unregister() {
        PushRegistrar.unregister(getContext());
    }

    @Override
    public void close() {
        PushRegistrar.onDestroy(getContext());
    }

    @Override
    public void onUnavailable() {
        PushRegistrar.onDestroy(getContext());
    }
}
