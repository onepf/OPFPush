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

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
class StubPushProvider extends BasePushProvider {

    private boolean mRegistered;
    private String mRegistrationId;

    StubPushProvider(@NotNull Context context) {
        super(context, "StubPushProvider", "org.onepf.openpush.sample");
    }

    @Override
    public void register() {
        mRegistered = true;
        mRegistrationId = UUID.randomUUID().toString();
        OpenPushHelper.getInstance(getContext()).onRegistrationEnd(
                new RegistrationResult(getName(), mRegistrationId));
    }

    @Override
    public void unregister() {
        mRegistrationId = null;
        mRegistered = false;
        OpenPushHelper.getInstance(getContext()).onUnregistrationEnd(
                new RegistrationResult(getName(), mRegistrationId));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isRegistered() {
        return mRegistered;
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        return mRegistrationId;
    }

    @Override
    public boolean checkManifest() {
        return true;
    }
}
