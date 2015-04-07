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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import org.onepf.opfpush.backoff.RetryManager;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.util.Set;

import static org.onepf.opfpush.OPFConstants.ACTION_RETRY_REGISTER;
import static org.onepf.opfpush.OPFConstants.ACTION_RETRY_UNREGISTER;

/**
 * @author Roman Savin
 * @since 07.04.2015
 */
public final class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        OPFLog.logMethod(context, OPFUtils.toString(intent));

        final Set<Pair<String, String>> retryProvidersActions = RetryManager.getInstance()
                .getRetryProvidersActions();

        final OPFPushHelper helper = OPFPush.getHelper();
        for (Pair<String, String> retryProviderAction : retryProvidersActions) {
            final String providerName = retryProviderAction.first;
            final String action = retryProviderAction.second;
            switch (action) {
                case ACTION_RETRY_REGISTER:
                    helper.register(providerName);
                    break;
                case ACTION_RETRY_UNREGISTER:
                    helper.unregister(providerName);
                    break;
            }
        }
    }
}
