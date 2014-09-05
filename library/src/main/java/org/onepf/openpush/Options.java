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

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Kirill Rozov
 * @since 04.09.2014
 */
public class Options {
    private final List<PushProvider> mProviders;

    private Options(List<PushProvider> providers) {
        mProviders = Collections.unmodifiableList(providers);
    }

    /**
     * Get all available push providers.
     *
     * @return List of all available push providers.
     */
    @NonNull
    public List<PushProvider> getProviders() {
        return mProviders;
    }

    /**
     * Helper class to create instance of {@link org.onepf.openpush.Options}.
     */
    public static class Builder {
        private static final int PROVIDERS_CAPACITY = 4;
        private List<PushProvider> mProviders;

        /**
         * Add the provider to the options.
         *
         * @param provider Provider to add.
         * @throws java.lang.IllegalArgumentException If try to add already added provider.
         */
        public void addProvider(@NonNull PushProvider provider) {
            if (mProviders != null && mProviders.contains(provider)) {
                throw new IllegalArgumentException(
                        String.format("Provider '%s' already added", provider));
            } else {
                if (mProviders == null) {
                    mProviders = new ArrayList<PushProvider>(PROVIDERS_CAPACITY);
                }
                mProviders.add(provider);
            }
        }

        /**
         * Create instance of {@link org.onepf.openpush.Options} with data from the builder.
         *
         * @return New options object.
         * @throws java.lang.IllegalArgumentException If no one provider added.
         */
        public Options build() {
            if (mProviders == null) {
                throw new IllegalArgumentException("Need to add at least one push provider.");
            }
            return new Options(mProviders);
        }
    }
}
