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
import org.onepf.opfpush.listener.CheckManifestHandler;
import org.onepf.opfpush.model.AvailabilityResult;
import org.onepf.opfpush.notification.NotificationMaker;
import org.onepf.opfpush.utils.CheckUtils;
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
    private final ADM adm = new ADM(getContext().getApplicationContext());

    @NonNull
    private final PreferencesProvider preferencesProvider =  PreferencesProvider.getInstance(getContext());

    public ADMProviderImpl(@NonNull final Context context) {
        super(context, PROVIDER_NAME, KINDLE_STORE_APP_PACKAGE);
    }

    public ADMProviderImpl(@NonNull final Context context, @NonNull final NotificationMaker notificationMaker) {
        super(context, PROVIDER_NAME, KINDLE_STORE_APP_PACKAGE, notificationMaker);
    }

    @Override
    public void register() {
        OPFLog.logMethod();
        OPFLog.i("Start register ADMProvider.");
        adm.startRegister();
    }

    @Override
    public void unregister() {
        OPFLog.logMethod();
        OPFLog.i("Start unregister ADMProvider.");
        adm.startUnregister();
    }

    @Override
    public void checkManifest(@Nullable final CheckManifestHandler checkManifestHandler) {
        OPFLog.logMethod();
        super.checkManifest(checkManifestHandler);
        final Context context = getContext();
        ADMManifest.checkManifestAuthoredProperly(context);
        CheckUtils.checkPermission(context, PERMISSION_RECEIVE_MESSAGES, checkManifestHandler);
        CheckUtils.checkPermission(context, GET_ACCOUNTS, checkManifestHandler);

        final String admMessagePermission = context.getPackageName() + RECEIVE_MESSAGE_PERMISSION_SUFFIX;
        CheckUtils.checkPermission(context, admMessagePermission, checkManifestHandler);

        CheckUtils.checkService(context, new ComponentName(context, ADMService.class), checkManifestHandler);

        final Intent registrationBroadcastIntent = new Intent(ACTION_APP_REGISTRATION_EVENT);
        final Intent receiveBroadcastIntent = new Intent(ACTION_RECEIVE_ADM_MESSAGE);
        final Intent loginChangedBroadcastIntent = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
        final String admReceiverName = ADMReceiver.class.getName();
        CheckUtils.checkReceiver(context, admReceiverName, registrationBroadcastIntent,
                ADMManifest.PERMISSION_SEND_MESSAGES, checkManifestHandler);
        CheckUtils.checkReceiver(context, admReceiverName, receiveBroadcastIntent,
                ADMManifest.PERMISSION_SEND_MESSAGES, checkManifestHandler);
        CheckUtils.checkReceiver(context, LoginAccountsChangedReceiver.class.getName(),
                loginChangedBroadcastIntent, checkManifestHandler);
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

    @NonNull
    @Override
    public AvailabilityResult getAvailabilityResult() {
        return new AvailabilityResult(
                super.getAvailabilityResult().isAvailable()
                        && adm.isSupported()
                        && Build.MANUFACTURER.equals(AMAZON_MANUFACTURER)
        );
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
