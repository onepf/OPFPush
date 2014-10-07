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

package org.onepf.opfpush;


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
 * Configuration object for {@link OPFPushHelper}.
 * You can create instance of this class with {@link Options.Builder}.
 *
 * @author Kirill Rozov
 * @since 04.09.2014
 */
public class Options {
    @NonNull
    private final List<PushProvider> mProviders;

    private final boolean mRecoverProvider;
    private final boolean mSelectSystemPreferred;

    @Nullable
    private final Backoff mBackoff;

    private Options(@NonNull Collection<? extends PushProvider> providers,
                    @Nullable Backoff backoff,
                    boolean recoverProvider,
                    boolean selectSystemPreferred) {
        mProviders = Collections.unmodifiableList(new ArrayList<PushProvider>(providers));
        mRecoverProvider = recoverProvider;
        mSelectSystemPreferred = selectSystemPreferred;
        mBackoff = backoff;
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

    public boolean isSelectSystemPreferred() {
        return mSelectSystemPreferred;
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

    @Nullable
    public Backoff getBackoff() {
        return mBackoff;
    }

    @Override
    public String toString() {
        return "Options{" +
                "providers=" + mProviders +
                ", recoverProvider=" + mRecoverProvider +
                ", systemPushPreferred=" + mSelectSystemPreferred +
                '}';
    }

    /**
     * Helper class to create instance of {@link org.onepf.opfpush.Options}.
     */
    public static class Builder {
        public static final int PROVIDERS_CAPACITY = 4;
        @Nullable
        private Map<String, PushProvider> mProviders;

        private boolean mRecoverProvider = true;
        private boolean mSelectSystemPreferred;

        @Nullable
        private Backoff mBackoff = new ExponentialBackoff();

        /**
         * Mark for try select the best store for device from added providers.
         * For Google device this is Google Cloud Messaging, for Kindle device - ADM.
         * If system has no preferred store or it isn't available push provider will be selected
         * be default algorithm.
         * <p/>
         * By default false.
         *
         * @param selectSystemPreferred Does select system preferred store.
         * @return The current {@code Builder}.
         */
        public Builder setSelectSystemPreferred(boolean selectSystemPreferred) {
            mSelectSystemPreferred = selectSystemPreferred;
            return this;
        }

        /**
         * Set does can the {@code OpenPushHelper} select next available provider,
         * when current became unavailable.
         * <p/>
         * By default true.
         *
         * @return The current {@code Builder}.
         */
        public Builder setRecoverProvider(boolean recoverProvider) {
            mRecoverProvider = recoverProvider;
            return this;
        }

        /**
         * Add the providers to the options.
         *
         * @param providers Providers to add.
         * @return The current {@code Builder}.
         * @throws java.lang.IllegalArgumentException If try to add already added providers.
         */
        @NonNull
        public Builder addProviders(@NonNull PushProvider... providers) {
            if (providers.length == 0) {
                return this;
            } else {
                return addProviders(Arrays.asList(providers));
            }
        }

        /**
         * Add the providers to the options.
         *
         * @param providers Providers to add.
         * @return The current {@code Builder}.
         * @throws java.lang.IllegalArgumentException If try to add already added providers.
         */
        @NonNull
        public Builder addProviders(@NonNull List<? extends PushProvider> providers) {
            if (providers.isEmpty()) {
                return this;
            }

            for (PushProvider provider : providers) {
                if (provider.isAvailable()) {
                    provider.checkManifest();
                }
            }

            if (mProviders == null) {
                mProviders = new LinkedHashMap<String, PushProvider>(PROVIDERS_CAPACITY);
            }

            for (PushProvider provider : providers) {
                final String providerName = provider.getName();
                if (mProviders.containsKey(providerName)) {
                    throw new IllegalArgumentException(
                            String.format("Provider '%s' already added.", provider));
                } else {
                    mProviders.put(providerName, provider);
                }
            }
            return this;
        }

        public Builder setBackoff(@Nullable Backoff backoff) {
            mBackoff = backoff;
            return this;
        }

        /**
         * Create instance of {@link Options} with data from the builder.
         *
         * @return New {@link Options} object.
         * @throws java.lang.IllegalArgumentException If no one provider added.
         */
        @NonNull
        public Options build() {
            if (mProviders == null) {
                throw new IllegalArgumentException("Need to add at least one push provider.");
            }
            return new Options(mProviders.values(), mBackoff, mRecoverProvider, mSelectSystemPreferred);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "providers=" + mProviders +
                    ", recoverProvider=" + mRecoverProvider +
                    ", systemPushPreferred=" + mSelectSystemPreferred +
                    '}';
        }
    }
}
