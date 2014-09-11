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
class MockPushProvider extends BasePushProvider {

    public static final String NAME = "StubPushProvider";
    private String mRegistrationId;

    MockPushProvider(@NotNull Context context) {
        super(context, NAME, "org.onepf.openpush.sample");
    }

    @Override
    public void register() {
        mRegistrationId = UUID.randomUUID().toString();
        OpenPushHelperKeeper.getInstance(getContext()).onRegistrationEnd(
                new RegistrationResult(NAME, mRegistrationId));
    }

    @Override
    public void unregister() {
        mRegistrationId = null;
        OpenPushHelperKeeper.getInstance(getContext()).onUnregistrationEnd(
                new RegistrationResult(NAME, mRegistrationId));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isRegistered() {
        return mRegistrationId != null;
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
