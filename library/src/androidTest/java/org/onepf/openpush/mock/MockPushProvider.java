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

package org.onepf.openpush.mock;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.Result;
import org.onepf.openpush.util.PackageUtils;
import org.robolectric.Robolectric;

import java.util.UUID;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
public class MockPushProvider extends BasePushProvider {

    public static final String DEFAULT_HOST_APP_PACKAGE = "org.onepf.store";
    private String mRegistrationId;
    private final boolean mAvailable;

    public MockPushProvider(@NonNull Context context) {
        this(context, MockPushProvider.class.getName());
    }

    public MockPushProvider(@NonNull Context context, String name) {
        this(context, name, true);
    }

    public MockPushProvider(@NonNull Context context,
                     @NonNull String name,
                     boolean available) {
        this(context, name, available, DEFAULT_HOST_APP_PACKAGE);
    }

    public MockPushProvider(@NonNull Context context,
                     @NonNull String name,
                     @NonNull String hotAppPackage) {
        this(context, name, true, hotAppPackage);
    }

    MockPushProvider(@NonNull Context context,
                     @NonNull String name,
                     boolean available,
                     @NonNull String hotAppPackage) {
        super(context, name, hotAppPackage);
        mAvailable = available;
    }

    @Override
    public void register() {
        mRegistrationId = UUID.randomUUID().toString();
        OpenPushHelper.getInstance(getContext())
                .onResult(new Result(getName(), mRegistrationId));
    }

    @Override
    public void unregister() {
        mRegistrationId = null;
        OpenPushHelper.getInstance(getContext())
                .onResult(new Result(getName(), mRegistrationId));
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
        return mAvailable && PackageUtils.isInstalled(Robolectric.application, getHostAppPackage());
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
