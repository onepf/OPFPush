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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.messaging.ADM;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.adm.ADMConstants.KINDLE_STORE_APP_PACKAGE;

/**
 * @author Roman Savin
 * @since 17.12.14
 */
final class ADMDelegate {

    @Nullable
    private ADM adm;

    public ADMDelegate(@NonNull final Context context) {
        OPFLog.methodD(context);

        if (OPFUtils.isInstalled(context, KINDLE_STORE_APP_PACKAGE)
                && isAdmClassExists()) {
            OPFLog.d("Kindle store app is installed");
            adm = new ADM(context.getApplicationContext());
        }
    }

    public boolean isSupported() {
        OPFLog.methodD();

        if (adm != null) {
            OPFLog.d("ADM isn't null");
            return adm.isSupported();
        }

        return false;
    }

    public void startRegister() {
        OPFLog.methodD();

        if (adm != null) {
            OPFLog.d("ADM isn't null");
            adm.startRegister();
        }
    }

    public void startUnregister() {
        OPFLog.methodD();

        if (adm != null) {
            OPFLog.d("ADM isn't null");
            adm.startUnregister();
        }
    }

    @Nullable
    public String getRegistrationId() {
        OPFLog.methodD();

        if (adm != null) {
            OPFLog.d("ADM isn't null");
            return adm.getRegistrationId();
        }

        return null;
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
