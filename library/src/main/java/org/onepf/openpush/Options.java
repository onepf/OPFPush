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

package org.onepf.openpush;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kirill Rozov
 * @since 04.09.2014
 */
public class Options {
    @NonNull
    private final List<PushProvider> mProviders;

    private final boolean mRecoverProvider;

    private final boolean mSystemPushPreferred;

    private Options(@NonNull Collection<? extends PushProvider> providers,
                    boolean recoverProvider,
                    boolean systemPushPreferred) {
        mProviders = Collections.unmodifiableList(new ArrayList<PushProvider>(providers));
        mRecoverProvider = recoverProvider;
        mSystemPushPreferred = systemPushPreferred;
    }

    /**
     * Can the {@code OpenPushHelper} select next available provider,
     * after current provider became unavailable.
     *
     * @return Can select next available provider for continue push work.
     */
    //TODO Find better name for this logic.
    public boolean isRecoverProvider() {
        return mRecoverProvider;
    }

    public boolean isSystemPushPreferred() {
        return mSystemPushPreferred;
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

    @Override
    public String toString() {
        return "Options{" +
                "providers=" + mProviders +
                ", recoverProvider=" + mRecoverProvider +
                ", systemPushPreferred=" + mSystemPushPreferred +
                '}';
    }

    /**
     * Helper class to create instance of {@link org.onepf.openpush.Options}.
     */
    public static class Builder {
        public static final int PROVIDERS_CAPACITY = 4;
        @Nullable
        private Map<String, PushProvider> mProviders;

        private boolean mRecoverProvider;

        private boolean mSystemPushPreferred;

        public Builder() {
            mRecoverProvider = true;
            mSystemPushPreferred = false;
        }

        /**
         * Add the providers to the options.
         *
         * @param providers Providers to add.
         * @return Current instance of builder.
         * @throws java.lang.IllegalArgumentException If try to add already added providers.
         */
        @NonNull
        public Builder addProviders(@NonNull PushProvider... providers) {
            if (providers.length == 0) {
                return this;
            }

            return addProviders(Arrays.asList(providers));
        }

        public Builder setSystemPushPreferred(boolean systemPushPreferred) {
            mSystemPushPreferred = systemPushPreferred;
            return this;
        }

        /**
         * Set does can the {@code OpenPushHelper} select next available provider,
         * when current became unavailable. By default true.
         *
         * @return The {@code Options.Builder}.
         */
        public Builder setRecoverProvider(boolean recoverProvider) {
            mRecoverProvider = recoverProvider;
            return this;
        }

        @NonNull
        public Builder addProviders(@NonNull List<? extends PushProvider> providers) {
            if (providers.isEmpty()) {
                return this;
            }

            if (mProviders == null) {
                mProviders = new LinkedHashMap<String, PushProvider>(PROVIDERS_CAPACITY);
            }

            for (PushProvider provider : providers) {
                final String providerName = provider.getName();
                if (mProviders.containsKey(providerName)) {
                    throw new IllegalArgumentException(
                            String.format("Provider '%s' already added.", provider));
                } else{
                    mProviders.put(providerName, provider);
                }
            }
            return this;
        }

        /**
         * Create instance of {@link org.onepf.openpush.Options} with data from the builder.
         *
         * @return New options object.
         * @throws java.lang.IllegalArgumentException If no one provider added.
         */
        @NonNull
        public Options build() {
            if (mProviders == null) {
                throw new IllegalArgumentException("Need to add at least one push provider.");
            }
            return new Options(mProviders.values(), mRecoverProvider, mSystemPushPreferred);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "providers=" + mProviders +
                    ", recoverProvider=" + mRecoverProvider +
                    ", systemPushPreferred=" + mSystemPushPreferred +
                    '}';
        }
    }
}
