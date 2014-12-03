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

package org.onepf.opfpush.mock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfpush.OPFPushHelper;
import org.robolectric.Robolectric;

import java.util.UUID;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 10/9/14.
 */
public class MockPushProvider extends BasePushProvider {

    public static final String DEFAULT_HOST_APP_PACKAGE = "org.onepf.store";
    public static final String DEFAULT_NAME = MockPushProvider.class.getName();
    private String registrationId;
    private boolean isTestAvailable = true;

    public MockPushProvider() {
        this(DEFAULT_NAME);
    }

    public MockPushProvider(String name) {
        this(name, true);
    }

    public MockPushProvider(@NonNull String name, boolean checkAvailability) {
        this(name, DEFAULT_HOST_APP_PACKAGE, checkAvailability);
    }

    public MockPushProvider(boolean isTestAvailable) {
        this(DEFAULT_NAME, DEFAULT_HOST_APP_PACKAGE, isTestAvailable);
    }

    public MockPushProvider(@NonNull String name,
                            @NonNull String hotAppPackage) {
        this(name, hotAppPackage, true);
    }

    public MockPushProvider(@NonNull String name,
                            @NonNull String hotAppPackage,
                            boolean isTestAvailable) {
        super(Robolectric.application, name, hotAppPackage);
        this.isTestAvailable = isTestAvailable;
    }

    @Override
    public void register() {
        registrationId = UUID.randomUUID().toString();
        OPFPushHelper.getInstance(getContext()).getReceivedMessageHandler()
                .onRegistered(getName(), registrationId);
    }

    @Override
    public void unregister() {
        final String oldRegistrationId = registrationId;
        registrationId = null;
        OPFPushHelper.getInstance(getContext()).getReceivedMessageHandler()
                .onUnregistered(getName(), oldRegistrationId);
    }

    /**
     * Set need to check availability of provider.
     * If availability don't check {@link #isAvailable()} always return {@code false}.
     */
    public void setTestAvailable(boolean isTestAvailable) {
        this.isTestAvailable = isTestAvailable;
    }

    @Override
    public void onRegistrationInvalid() {
        registrationId = null;
    }

    @Override
    public void onUnavailable() {
        registrationId = null;
    }

    @Override
    public boolean isAvailable() {
        return isTestAvailable && super.isAvailable();
    }

    @Override
    public boolean isRegistered() {
        return registrationId != null;
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        return registrationId;
    }
}
