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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kirill Rozov
 * @since 04.09.2014
 */
public class Options {
    private final List<PushProvider> mProviders;
    private final Backoff mBackoff;

    private Options(@NotNull Collection<PushProvider> providers, @Nullable Backoff backoff) {
        mProviders = Collections.unmodifiableList(new ArrayList<PushProvider>(providers));
        mBackoff = backoff;
    }

    /**
     * Get all available push providers.
     *
     * @return List of all available push providers.
     */
    @NotNull
    public List<PushProvider> getProviders() {
        return mProviders;
    }

    @Nullable
    public Backoff getBackoff() {
        return mBackoff;
    }

    /**
     * Helper class to create instance of {@link org.onepf.openpush.Options}.
     */
    public static class Builder {
        private Set<PushProvider> mProviders;
        private Backoff mBackoff;

        /**
         * Add the provider to the options.
         *
         * @param provider Provider to add.
         * @return Current instance of builder.
         * @throws java.lang.IllegalArgumentException If try to add already added provider.
         */
        public Builder addProvider(@NotNull PushProvider provider) {
            if (mProviders == null) {
                mProviders = new HashSet<PushProvider>(4);
            }

            if (mProviders.add(provider)) {
                throw new IllegalArgumentException(
                        String.format("Provider '%s' already added", provider));
            }
            return this;
        }

        public Builder setBackoff(@Nullable Backoff backoff) {
            mBackoff = backoff;
            return this;
        }

        /**
         * Create instance of {@link org.onepf.openpush.Options} with data from the builder.
         *
         * @return New options object.
         * @throws java.lang.IllegalArgumentException If no one provider added.
         */
        @NotNull
        public Options build() {
            if (mProviders == null) {
                throw new IllegalArgumentException("Need to add at least one push provider.");
            }
            return new Options(mProviders, mBackoff);
        }
    }
}
