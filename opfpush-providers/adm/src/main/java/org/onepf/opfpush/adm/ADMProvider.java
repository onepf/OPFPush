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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.listener.CheckManifestHandler;
import org.onepf.opfpush.model.AvailabilityResult;
import org.onepf.opfpush.notification.NotificationMaker;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfutils.OPFLog;

import static org.onepf.opfpush.adm.ADMConstants.AMAZON_MANUFACTURER;

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
        if (Build.MANUFACTURER.equals(AMAZON_MANUFACTURER)) {
            OPFLog.d("It's an Amazon device.");
            provider = new ADMProviderImpl(context.getApplicationContext());
        } else {
            OPFLog.d("It's no an Amazon device.");
            provider = new ADMProviderStub();
        }
    }

    public ADMProvider(@NonNull final Context context,
                       @NonNull final NotificationMaker notificationMaker) {
        if (Build.MANUFACTURER.equals(AMAZON_MANUFACTURER)) {
            OPFLog.d("It's an Amazon device.");
            provider = new ADMProviderImpl(context.getApplicationContext(), notificationMaker);
        } else {
            OPFLog.d("It's no an Amazon device.");
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

    @NonNull
    @Override
    public AvailabilityResult getAvailabilityResult() {
        return provider.getAvailabilityResult();
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

    @NonNull
    @Override
    public NotificationMaker getNotificationMaker() {
        return provider.getNotificationMaker();
    }

    @Override
    public void checkManifest(@Nullable final CheckManifestHandler checkManifestHandler) {
        provider.checkManifest(checkManifestHandler);
    }

    @Override
    public void onRegistrationInvalid() {
        provider.onRegistrationInvalid();
    }

    @Override
    public void onUnavailable() {
        provider.onUnavailable();
    }
}
