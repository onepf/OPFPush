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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.RetryBroadcastReceiver;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.OPFConstants.ACTION_RETRY_UNREGISTER;
import static org.onepf.opfpush.OPFConstants.EXTRA_PROVIDER_NAME;
import static org.onepf.opfpush.adm.ADMConstants.ACCOUNT_TYPE;
import static org.onepf.opfpush.adm.ADMConstants.PROVIDER_NAME;

/**
 * @author Roman Savin
 * @since 03.02.2015
 */
public class LoginAccountsChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFPushLog.methodD(LoginAccountsChangedReceiver.class, "onReceive", context, OPFUtils.toString(intent));

        final PreferencesProvider preferencesProvider = PreferencesProvider.getInstance(context);
        final Account[] amazonAccounts = AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE);
        if (amazonAccounts.length != 0 && preferencesProvider.isAuthenticationFailed()) {
            OPFPushLog.d("Retry unregister");
            preferencesProvider.removeAuthenticationFailedFlag();

            final Intent retryUnregisterIntent = new Intent(context, RetryBroadcastReceiver.class);
            retryUnregisterIntent.setAction(ACTION_RETRY_UNREGISTER);
            retryUnregisterIntent.putExtra(EXTRA_PROVIDER_NAME, PROVIDER_NAME);
            context.sendBroadcast(retryUnregisterIntent);
        }
    }
}
