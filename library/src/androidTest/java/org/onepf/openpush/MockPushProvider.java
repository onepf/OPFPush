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
import org.onepf.openpush.util.OpenPushHelperKeeper;

import java.util.UUID;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
class MockPushProvider extends BasePushProvider {

    private String mRegistrationId;
    private final boolean mAvailable;

    private boolean mHostAppEnable = true;

    MockPushProvider(@NotNull Context context) {
        this(context, MockPushProvider.class.getName());
    }

    MockPushProvider(@NotNull Context context, String name) {
        this(context, name, true);
    }

    MockPushProvider(@NotNull Context context,
                     @NotNull String name,
                     boolean available) {
        this(context, name, available, "org.onepf.store");
    }

    MockPushProvider(@NotNull Context context,
                     @NotNull String name,
                     @NotNull String hotAppPackage) {
        this(context, name, true, hotAppPackage);
    }

    MockPushProvider(@NotNull Context context,
                     @NotNull String name,
                     boolean available,
                     @NotNull String hotAppPackage) {
        super(context, name, hotAppPackage);
        mAvailable = available;
    }

    public void setHostAppEnable(boolean hostAppEnable) {
        mHostAppEnable = hostAppEnable;
    }

    @Override
    public void register() {
        mRegistrationId = UUID.randomUUID().toString();
        OpenPushHelperKeeper.getInstance(getContext()).onRegistrationEnd(
                new RegistrationResult(getName(), mRegistrationId));
    }

    @Override
    public void unregister() {
        mRegistrationId = null;
        OpenPushHelperKeeper.getInstance(getContext()).onUnregistrationEnd(
                new RegistrationResult(getName(), mRegistrationId));
    }

    @Override
    public void onAppStateChanged() {
        mRegistrationId = null;
    }

    @Override
    public void onUnavailable() {
        mRegistrationId = null;
    }

    @Override
    public boolean isAvailable() {
        return mHostAppEnable && mAvailable;
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