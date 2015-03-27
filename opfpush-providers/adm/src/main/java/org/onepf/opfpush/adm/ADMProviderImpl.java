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

import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.development.ADMManifest;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import static android.Manifest.permission.GET_ACCOUNTS;
import static com.amazon.device.messaging.ADMConstants.LowLevel.ACTION_APP_REGISTRATION_EVENT;
import static com.amazon.device.messaging.ADMConstants.LowLevel.ACTION_RECEIVE_ADM_MESSAGE;
import static com.amazon.device.messaging.development.ADMManifest.PERMISSION_RECEIVE_MESSAGES;
import static org.onepf.opfpush.adm.ADMConstants.AMAZON_MANUFACTURER;
import static org.onepf.opfpush.adm.ADMConstants.KINDLE_STORE_APP_PACKAGE;
import static org.onepf.opfpush.adm.ADMConstants.PROVIDER_NAME;
import static org.onepf.opfpush.adm.ADMConstants.RECEIVE_MESSAGE_PERMISSION_SUFFIX;

/**
 * Amazon Device Messaging push provider implementation.
 *
 * @author Roman Savin
 * @since 23.03.2015
 */
class ADMProviderImpl extends BasePushProvider {

    @NonNull
    private final ADM adm;

    private final PreferencesProvider preferencesProvider;

    public ADMProviderImpl(@NonNull final Context context) {
        super(context, PROVIDER_NAME, KINDLE_STORE_APP_PACKAGE);
        adm = new ADM(context.getApplicationContext());
        preferencesProvider = PreferencesProvider.getInstance(getContext());
    }

    @Override
    public void register() {
        super.register();
        OPFLog.logMethod();
        if (!isUnregistrationPerforming()) {
            OPFLog.i("Start register ADMProvider.");
            adm.startRegister();
        }
    }

    @Override
    public void unregister() {
        super.unregister();
        OPFLog.logMethod();
        if (!isRegistrationPerforming()) {
            OPFLog.i("Start unregister ADMProvider.");
            adm.startUnregister();
        }
    }

    @Override
    public void checkManifest() {
        OPFLog.logMethod();
        super.checkManifest();
        final Context context = getContext();
        ADMManifest.checkManifestAuthoredProperly(context);
        OPFChecks.checkPermission(context, PERMISSION_RECEIVE_MESSAGES);
        OPFChecks.checkPermission(context, GET_ACCOUNTS);

        final String admMessagePermission = context.getPackageName() + RECEIVE_MESSAGE_PERMISSION_SUFFIX;
        OPFChecks.checkPermission(context, admMessagePermission);

        OPFChecks.checkService(context, new ComponentName(context, ADMService.class));

        final Intent registrationBroadcastIntent = new Intent(ACTION_APP_REGISTRATION_EVENT);
        final Intent receiveBroadcastIntent = new Intent(ACTION_RECEIVE_ADM_MESSAGE);
        final Intent loginChangedBroadcastIntent = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
        final String admReceiverName = ADMReceiver.class.getName();
        OPFChecks.checkReceiver(context, admReceiverName, registrationBroadcastIntent,
                ADMManifest.PERMISSION_SEND_MESSAGES);
        OPFChecks.checkReceiver(context, admReceiverName, receiveBroadcastIntent,
                ADMManifest.PERMISSION_SEND_MESSAGES);
        OPFChecks.checkReceiver(context, LoginAccountsChangedReceiver.class.getName(),
                loginChangedBroadcastIntent);
    }

    @Override
    public void onRegistrationInvalid() {
        //nothing
    }

    @Override
    public void onUnavailable() {
        //nothing
    }

    @Override
    public boolean isRegistered() {
        OPFLog.logMethod();
        return !TextUtils.isEmpty(getRegistrationId());
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable()
                && adm.isSupported()
                && Build.MANUFACTURER.equals(AMAZON_MANUFACTURER);
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        OPFLog.logMethod();
        if (!TextUtils.isEmpty(adm.getRegistrationId())) {
            OPFLog.d("ADM registration id is not empty");
            return adm.getRegistrationId();
        }

        return preferencesProvider.getRegistrationId();
    }

    @NonNull
    @Override
    public String toString() {
        return PROVIDER_NAME;
    }
}
