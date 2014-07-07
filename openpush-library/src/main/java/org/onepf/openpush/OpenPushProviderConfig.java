/*******************************************************************************
 * Copyright 2014 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

package org.onepf.openpush;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class OpenPushProviderConfig {

    private static final String TAG = OpenPushProviderConfig.class.getSimpleName();

    final PushListener pushListener;
    final Context appContext;
    final List<PushProvider> selectedProviders = new ArrayList<PushProvider>();

    public OpenPushProviderConfig(Context context, PushListener listener) {
        appContext = context.getApplicationContext();
        pushListener = listener;
    }

    /**
     *
     * @param provider
     */
    public void addProvider(PushProvider provider) {
        if (!alreadyAdded(selectedProviders, provider)) {
            selectedProviders.add(provider);
        } else {
            if (OpenPushLog.isEnabled()) {
                Log.w(TAG, "Attempt to add same provider twice: " + provider.getName() + ". Ignoring...");
            }
        }
    }

    private boolean alreadyAdded(List<PushProvider> selectedProviders, PushProvider providerToAdd) {
        // Ignoring same provider, even if it has different senderIDs for some reason
        for (PushProvider provider: selectedProviders) {
            if (provider.getName().equals(providerToAdd.getName())) {
                return true;
            }
        }

        return false;
    }

}
