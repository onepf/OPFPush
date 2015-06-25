/*
 * Copyright 2012-2015 One Platform Foundation
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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfutils.OPFLog;

/**
 * Stub class for non main processes.
 *
 * @author Roman Savin
 * @since 20.03.2015
 */
@SuppressWarnings("PMD.TooManyMethods")
public class OPFPushHelperStub extends OPFPushHelper {

    @Override
    public void register() {
        OPFLog.logMethod();
    }

    @Override
    public void unregister() {
        OPFLog.logMethod();
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        OPFLog.logMethod();
        return null;
    }

    @Nullable
    @Override
    public String getProviderName() {
        OPFLog.logMethod();
        return null;
    }

    @Nullable
    @Override
    public PushProvider getCurrentProvider() {
        OPFLog.logMethod();
        return null;
    }

    @NonNull
    @Override
    public ReceivedMessageHandler getReceivedMessageHandler() {
        OPFLog.logMethod();
        return new ReceivedMessageHandlerStub();
    }

    @Override
    public boolean isRegistered() {
        return false;
    }

    @Override
    public boolean isRegistering() {
        return false;
    }

    @Override
    public void onNeedRetryRegistration() {
        OPFLog.logMethod();
    }

    @Override
    void init(@NonNull final Configuration initialConfiguration) {
        OPFLog.logMethod(initialConfiguration);
    }

    @Override
    boolean isInitDone() {
        OPFLog.logMethod();
        return false;
    }

    @Override
    void restartRegisterOnBoot() {
        OPFLog.logMethod();
    }

    @Override
    void register(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
    }

    @Override
    void unregister(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
    }

    @Override
    void registerNextAvailableProvider(@Nullable final String prevProviderName) {
        OPFLog.logMethod(prevProviderName);
    }

    @Nullable
    @Override
    String getProviderNameByHostApp(@Nullable final String appPackage) {
        OPFLog.logMethod(appPackage);
        return null;
    }

    private final class ReceivedMessageHandlerStub implements ReceivedMessageHandler {

        @Override
        public void onMessage(@NonNull final String providerName,
                              @Nullable final Bundle extras) {
            OPFLog.logMethod(providerName, extras);
        }

        @Override
        public void onDeletedMessages(@NonNull final String providerName,
                                      final int messagesCount) {
            OPFLog.logMethod(providerName, messagesCount);
        }

        @Override
        public void onRegistered(@NonNull final String providerName,
                                 @NonNull final String registrationId) {
            OPFLog.logMethod(providerName, registrationId);
        }

        @Override
        public void onUnregistered(@NonNull final String providerName,
                                   @Nullable final String oldRegistrationId) {
            OPFLog.logMethod(providerName, oldRegistrationId);
        }

        @Override
        public void onRegistrationError(@NonNull final String providerName,
                                        @NonNull final PushError error) {
            OPFLog.logMethod(providerName, error);
        }

        @Override
        public void onUnregistrationError(@NonNull final String providerName,
                                          @NonNull final PushError error) {
            OPFLog.logMethod(providerName, error);
        }

        @Override
        public void onError(@NonNull final String providerName,
                            @NonNull final PushError error) {
            OPFLog.logMethod(providerName, error);
        }
    }
}
