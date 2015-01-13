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

package org.onepf.opfpush.adm;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.messaging.development.ADMManifest;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfpush.OPFPushLog;

import static org.onepf.opfpush.adm.Constants.AMAZON_MANUFACTURER;
import static org.onepf.opfpush.adm.Constants.KINDLE_STORE_APP_PACKAGE;
import static org.onepf.opfpush.adm.Constants.NAME;
import static org.onepf.opfutils.OPFUtils.hasRequestedPermission;

/**
 * Amazon Device Messaging push provider implementation.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @see <a href="https://developer.amazon.com/appsandservices/apis/engage/device-messaging">Amazon Device Messaging</a>
 * @since 06.09.14
 */
public class ADMProvider extends BasePushProvider {

    @NonNull
    private ADMDelegate adm;

    public ADMProvider(@NonNull final Context context) {
        super(context, NAME, KINDLE_STORE_APP_PACKAGE);
        adm = new ADMDelegate(context);
    }

    @Override
    public void register() {
        OPFPushLog.methodD(ADMProvider.class, "register");

        if (!isRegistered()) {
            OPFPushLog.d("Start register ADMProvider.");
            adm.startRegister();
        }
    }

    @Override
    public boolean checkManifest() {
        OPFPushLog.methodD(ADMProvider.class, "checkManifest");
        //TODO Maybe better use ADMManifest.checkManifestAuthoredProperly(getContext())?
        if (super.checkManifest() && Build.MANUFACTURER.equals(AMAZON_MANUFACTURER)) {
            final Context context = getContext();
            return super.checkManifest()
                    && hasRequestedPermission(context, android.Manifest.permission.RECEIVE_BOOT_COMPLETED)
                    && hasRequestedPermission(context, ADMManifest.PERMISSION_RECEIVE_MESSAGES)
                    && hasRequestedPermission(context, context.getPackageName() + ".permission.RECEIVE_ADM_MESSAGE");
        } else {
            return false;
        }
    }

    @Override
    public boolean isRegistered() {
        OPFPushLog.methodD(ADMProvider.class, "isRegistered");
        return adm.getRegistrationId() != null;
    }

    @Override
    public void unregister() {
        OPFPushLog.methodD(ADMProvider.class, "unregister");
        adm.startUnregister();
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable() && adm.isSupported();
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return adm.getRegistrationId();
    }
}
