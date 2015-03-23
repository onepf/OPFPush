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

package org.onepf.opfpush.nokia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfutils.OPFLog;

import static org.onepf.opfpush.nokia.NokiaPushConstants.PROVIDER_NAME;

/**
 * Nokia Notification push provider stub for not nokia devices.
 *
 * @author Roman Savin
 * @since 23.03.2015
 */
class NokiaNotificationsProviderStub implements NokiaPushProvider {

    @Override
    public long getRegisterOnServerLifespan() {
        OPFLog.methodD();
        return 0;
    }

    @Override
    public void setRegisteredOnServer(@NonNull final Context context, final boolean flag) {
        OPFLog.methodD(context, flag);
    }

    @Override
    public void setRegisterOnServerLifespan(@NonNull final Context context, final long lifespan) {
        OPFLog.methodD(context, lifespan);
    }

    @Override
    public boolean isRegisterOnServer() {
        OPFLog.methodD();
        return false;
    }

    @Override
    public void register() {
        OPFLog.methodD();
    }

    @Override
    public void unregister() {
        OPFLog.methodD();
    }

    @Override
    public boolean isAvailable() {
        OPFLog.methodD();
        return false;
    }

    @Override
    public boolean isRegistered() {
        OPFLog.methodD();
        return false;
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        OPFLog.methodD();
        return null;
    }

    @NonNull
    @Override
    public String getName() {
        OPFLog.methodD();
        return PROVIDER_NAME;
    }

    @Nullable
    @Override
    public String getHostAppPackage() {
        OPFLog.methodD();
        return null;
    }

    @Override
    public void checkManifest() {
        OPFLog.methodD();
    }

    @Override
    public void onRegistrationInvalid() {
        OPFLog.methodD();
    }

    @Override
    public void onUnavailable() {
        OPFLog.methodD();
    }
}
