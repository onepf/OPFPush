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

package org.onepf.opfpush.adm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.listener.CheckManifestHandler;
import org.onepf.opfpush.model.AvailabilityResult;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfutils.OPFLog;

import static org.onepf.opfpush.adm.ADMConstants.PROVIDER_NAME;

/**
 * Amazon Device Messaging push provider stub for not amazon devices.
 *
 * @author Roman Savin
 * @since 23.03.2015
 */
class ADMProviderStub implements PushProvider {

    @Override
    public void register() {
        OPFLog.logMethod();
    }

    @Override
    public void unregister() {
        OPFLog.logMethod();
    }

    @NonNull
    @Override
    public AvailabilityResult getAvailabilityResult() {
        OPFLog.logMethod();
        return new AvailabilityResult(false);
    }

    @Override
    public boolean isRegistered() {
        OPFLog.logMethod();
        return false;
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        OPFLog.logMethod();
        return null;
    }

    @NonNull
    @Override
    public String getName() {
        OPFLog.logMethod();
        return PROVIDER_NAME;
    }

    @Nullable
    @Override
    public String getHostAppPackage() {
        OPFLog.logMethod();
        return null;
    }

    @Override
    public void checkManifest(@Nullable final CheckManifestHandler checkManifestHandler) {
        OPFLog.logMethod();
    }

    @Override
    public void onRegistrationInvalid() {
        OPFLog.logMethod();
    }

    @Override
    public void onUnavailable() {
        OPFLog.logMethod();
    }
}
