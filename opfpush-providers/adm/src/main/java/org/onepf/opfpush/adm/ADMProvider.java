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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.messaging.ADM;

import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.adm.ADMConstants.KINDLE_STORE_APP_PACKAGE;

/**
 * Amazon Device Messaging push provider delegate.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @see <a href="https://developer.amazon.com/appsandservices/apis/engage/device-messaging">Amazon Device Messaging</a>
 * @since 06.09.14
 */
public class ADMProvider implements PushProvider {

    @NonNull
    private final PushProvider provider;

    public ADMProvider(@NonNull final Context context) {
        if (OPFUtils.isInstalled(context, KINDLE_STORE_APP_PACKAGE)
                && isAdmClassExists()) {
            OPFLog.d("Kindle store app is installed");
            provider = new ADMProviderImpl(context.getApplicationContext());
        } else {
            OPFLog.d("Kindle store app isn't installed");
            provider = new ADMProviderStub();
        }
    }

    @Override
    public void register() {
        provider.register();
    }

    @Override
    public void unregister() {
        provider.unregister();
    }

    @Override
    public boolean isAvailable() {
        return provider.isAvailable();
    }

    @Override
    public boolean isRegistered() {
        return provider.isRegistered();
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        return provider.getRegistrationId();
    }

    @NonNull
    @Override
    public String getName() {
        return provider.getName();
    }

    @Nullable
    @Override
    public String getHostAppPackage() {
        return provider.getHostAppPackage();
    }

    @Override
    public void checkManifest() {
        provider.checkManifest();
    }

    @Override
    public void onRegistrationInvalid() {
        provider.onRegistrationInvalid();
    }

    @Override
    public void onUnavailable() {
        provider.onUnavailable();
    }

    private boolean isAdmClassExists() {
        try {
            ADM.class.getCanonicalName();
            return true;
        } catch (NoClassDefFoundError e) {
            OPFLog.w("ADM class not found");
        }

        return false;
    }
}
