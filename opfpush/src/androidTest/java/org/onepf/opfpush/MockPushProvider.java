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

package org.onepf.opfpush;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.robolectric.Robolectric;

import java.util.UUID;

/**
* @author Kirill Rozov
* @since 10/9/14.
*/
public class MockPushProvider extends BasePushProvider {

    public static final String DEFAULT_HOST_APP_PACKAGE = "org.onepf.store";
    public static final String DEFAULT_NAME = MockPushProvider.class.getName();
    private String mRegistrationId;
    private boolean mCheckAvailability = true;

    public MockPushProvider() {
        this(DEFAULT_NAME);
    }

    public MockPushProvider(String name) {
        this(name, true);
    }

    public MockPushProvider(@NonNull String name, boolean checkAvailability) {
        this(name, DEFAULT_HOST_APP_PACKAGE, checkAvailability);
    }

    public MockPushProvider(boolean checkAvailability) {
        this(DEFAULT_NAME, DEFAULT_HOST_APP_PACKAGE, checkAvailability);
    }

    public MockPushProvider(@NonNull String name,
                            @NonNull String hotAppPackage) {
        this(name, hotAppPackage, true);
    }

    public MockPushProvider(@NonNull String name,
                            @NonNull String hotAppPackage,
                            boolean checkAvailability) {
        super(Robolectric.application, name, hotAppPackage);
        mCheckAvailability = checkAvailability;
    }

    @Override
    public void register() {
        mRegistrationId = UUID.randomUUID().toString();
        OPFPushHelper.getInstance(getContext()).getReceivedMessageHandler()
                .onRegistered(getName(), mRegistrationId);
    }

    @Override
    public void unregister() {
        final String oldRegistrationId = mRegistrationId;
        mRegistrationId = null;
        OPFPushHelper.getInstance(getContext()).getReceivedMessageHandler()
                .onUnregistered(getName(), oldRegistrationId);
    }

    /**
     * Set need to check availability of provider.
     * If availability don't check {@link #isAvailable()} always return {@code false}.
     */
    public void setCheckAvailability(boolean checkAvailability) {
        mCheckAvailability = checkAvailability;
    }

    @Override
    public void onRegistrationInvalid() {
        mRegistrationId = null;
    }

    @Override
    public void onUnavailable() {
        mRegistrationId = null;
    }

    @Override
    public boolean isAvailable() {
        return mCheckAvailability && super.isAvailable();
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
