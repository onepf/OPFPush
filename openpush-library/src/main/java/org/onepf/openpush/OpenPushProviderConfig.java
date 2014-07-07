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

    /**
     * Base constructor for the configuration
     * @param context context of any type
     * @param listener listener for push provider actions
     */
    public OpenPushProviderConfig(Context context, PushListener listener) {
        appContext = context.getApplicationContext();
        pushListener = listener;
    }

    /**
     * Adds the provider to the configuration.
     * If the provider is already contained, it will be added.
     * @param provider provider to add
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

    /**
     * Internal method to check whether a provider is already added.
     * @param selectedProviders  set of the added providers
     * @param providerToAdd provider to check
     * @return is the provider already added
     */
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
