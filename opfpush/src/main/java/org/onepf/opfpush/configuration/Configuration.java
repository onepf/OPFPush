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

package org.onepf.opfpush.configuration;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.PushProvider;
import org.onepf.opfpush.listener.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration object for {@link org.onepf.opfpush.OPFPushHelper}.
 * You can create instance of this class with {@link Configuration.Builder}.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 04.09.2014
 */
public final class Configuration {

    @NonNull
    private final List<PushProvider> providers;

    @Nullable
    private final EventListener eventListener;

    @NonNull
    private final Backoff backoff;

    private final boolean isSelectSystemPreferred;

    private Configuration(@NonNull final Collection<? extends PushProvider> providers,
                          @Nullable final EventListener eventListener,
                          @NonNull final Backoff backoff,
                          final boolean selectSystemPreferred) {

        this.providers = Collections.unmodifiableList(new ArrayList<>(providers));
        this.eventListener = eventListener;
        this.backoff = backoff;
        this.isSelectSystemPreferred = selectSystemPreferred;
    }

    /**
     * Get all available push providers.
     *
     * @return List of all available push providers.
     */
    @NonNull
    public List<PushProvider> getProviders() {
        return providers;
    }

    /**
     * Get the {@code EventListener}.
     *
     * @return event listener of push providers.
     */
    @Nullable
    public EventListener getEventListener() {
        return eventListener;
    }

    @NonNull
    public Backoff getBackoff() {
        return backoff;
    }

    public boolean isSelectSystemPreferred() {
        return isSelectSystemPreferred;
    }

    @Override
    public String toString() {
        return "Configuration {"
                + "providers = " + providers
                + ", isSelectSystemPreferred = " + isSelectSystemPreferred
                + '}';
    }

    /**
     * Helper class to create instance of {@link Configuration}.
     */
    public static final class Builder {

        @Nullable
        private Map<String, PushProvider> providersMap;

        @Nullable
        private EventListener eventListener;

        @NonNull
        private Backoff backoff = new ExponentialBackoff();

        private boolean isSelectSystemPreferred = true;

        /**
         * Add the providers to the configuration.
         *
         * @param providers Providers to add.
         * @return The current {@code Builder}.
         * @throws java.lang.IllegalArgumentException If try to add already added providers.
         */
        @NonNull
        public Builder addProviders(@NonNull final PushProvider... providers) {
            if (providers.length == 0) {
                return this;
            } else {
                return addProviders(Arrays.asList(providers));
            }
        }

        /**
         * Add the providers to the configuration.
         *
         * @param providers Providers to add.
         * @return The current {@code Builder}.
         * @throws java.lang.IllegalArgumentException If try to add already added providers.
         */
        @NonNull
        public Builder addProviders(@NonNull final List<? extends PushProvider> providers) {
            if (providers.isEmpty()) {
                return this;
            }

            for (PushProvider provider : providers) {
                if (provider.isAvailable()) {
                    provider.checkManifest();
                }
            }

            if (this.providersMap == null) {
                this.providersMap = new LinkedHashMap<>();
            }

            for (PushProvider provider : providers) {
                final String providerName = provider.getName();
                if (this.providersMap.containsKey(providerName)) {
                    throw new IllegalArgumentException(
                            String.format("Provider '%s' already added.", provider)
                    );
                } else {
                    this.providersMap.put(providerName, provider);
                }
            }
            return this;
        }

        @NonNull
        public Builder setEventListener(@NonNull final EventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public Builder setBackoff(@NonNull Backoff backoff) {
            this.backoff = backoff;
            return this;
        }

        /**
         * Mark for try select the best store for device from added providers.
         * For Google device this is Google Cloud Messaging, for Kindle device - ADM.
         * If system has no preferred store or it isn't available push provider will be selected
         * be default algorithm.
         * <p/>
         * By default false.
         *
         * @param isSelectSystemPreferred Does select system preferred store.
         * @return The current {@code Builder}.
         */
        public Builder setSelectSystemPreferred(final boolean isSelectSystemPreferred) {
            this.isSelectSystemPreferred = isSelectSystemPreferred;
            return this;
        }

        /**
         * Create instance of {@link Configuration} with data from the builder.
         *
         * @return New {@link Configuration} object.
         * @throws java.lang.IllegalArgumentException If no one provider added.
         */
        @NonNull
        public Configuration build() {
            if (providersMap == null) {
                throw new IllegalArgumentException("Need to add at least one push provider.");
            }

            return new Configuration(
                    providersMap.values(),
                    eventListener,
                    backoff,
                    isSelectSystemPreferred
            );
        }

        @Override
        public String toString() {
            return "Builder{"
                    + "providersMap="
                    + providersMap
                    + ", systemPushPreferred="
                    + isSelectSystemPreferred
                    + '}';
        }
    }
}
