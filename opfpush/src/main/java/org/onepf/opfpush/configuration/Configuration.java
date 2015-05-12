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

package org.onepf.opfpush.configuration;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.listener.CheckManifestHandler;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.pushprovider.PushProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The instance of this class is used as an argument of the {@link org.onepf.opfpush.OPFPush#init(android.content.Context, Configuration)}
 * method for configuring the {@link org.onepf.opfpush.OPFPushHelper} instance.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 04.09.2014
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class Configuration {

    @NonNull
    private final List<PushProvider> providers;

    @Nullable
    private final EventListener eventListener;

    @Nullable
    private final CheckManifestHandler checkManifestHandler;

    private final boolean isSelectSystemPreferred;

    private Configuration(@NonNull final Collection<? extends PushProvider> providers,
                          @Nullable final EventListener eventListener,
                          final boolean selectSystemPreferred,
                          @Nullable CheckManifestHandler checkManifestHandler) {
        this.providers = Collections.unmodifiableList(new ArrayList<>(providers));
        this.eventListener = eventListener;
        this.isSelectSystemPreferred = selectSystemPreferred;
        this.checkManifestHandler = checkManifestHandler;
    }

    /**
     * Returns all available push providers.
     *
     * @return All available push providers.
     */
    @NonNull
    public List<PushProvider> getProviders() {
        return providers;
    }

    /**
     * Returns the instance of the {@link org.onepf.opfpush.listener.EventListener}.
     *
     * @return The instance of the {@link org.onepf.opfpush.listener.EventListener}.
     */
    @Nullable
    public EventListener getEventListener() {
        return eventListener;
    }

    /**
     * Returns {@code true} if the system push provider is preferred, false otherwise.
     *
     * @return {@code true} if the system push provider is preferred, false otherwise.
     */
    public boolean isSelectSystemPreferred() {
        return isSelectSystemPreferred;
    }

    /**
     * Returns the instance of the {@link CheckManifestHandler}.
     *
     * @return The instance of the {@link CheckManifestHandler}.
     */
    @Nullable
    public CheckManifestHandler getCheckManifestHandler() {
        return checkManifestHandler;
    }

    @Override
    public String toString() {
        return "Configuration {"
                + "providers = " + providers
                + ", isSelectSystemPreferred = " + isSelectSystemPreferred
                + '}';
    }

    /**
     * The builder class that creates an instance of the {@code Configuration} class.
     */
    public static final class Builder {

        @Nullable
        private Map<String, PushProvider> providersMap;

        @Nullable
        private EventListener eventListener;

        private boolean isSelectSystemPreferred;

        @Nullable
        private CheckManifestHandler checkManifestHandler;

        /**
         * See {@link #addProviders(java.util.List)}
         *
         * @return The current {@code Builder} instance.
         * @throws java.lang.IllegalArgumentException If a provider was already added
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
         * Add push providers to the configuration. The priority of the providers corresponds to the order
         * in which they were added.
         *
         * @return The current {@code Builder} instance.
         * @throws java.lang.IllegalArgumentException If a provider was already added.
         */
        @NonNull
        public Builder addProviders(@NonNull final List<? extends PushProvider> providers) {
            if (providers.isEmpty()) {
                return this;
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

        /**
         * If you set {@code true}, the system push provider will get the highest priority.
         * For Google devices this is Google Cloud Messaging, for Kindle devices - ADM.
         * False by default.
         *
         * @param isSelectSystemPreferred {@code true} if the system provider is preferred, {@code false} otherwise.
         * @return The current {@code Builder}.
         */
        public Builder setSelectSystemPreferred(final boolean isSelectSystemPreferred) {
            this.isSelectSystemPreferred = isSelectSystemPreferred;
            return this;
        }

        @NonNull
        public Builder setCheckManifestHandler(@NonNull final CheckManifestHandler checkManifestHandler) {
            this.checkManifestHandler = checkManifestHandler;
            return this;
        }

        /**
         * Create the instance of the {@link Configuration} class.
         *
         * @return The new {@link Configuration} object.
         * @throws java.lang.IllegalArgumentException If there are no any added providers.
         */
        @NonNull
        @SuppressWarnings("PMD.AccessorClassGeneration")
        public Configuration build() {
            if (providersMap == null) {
                throw new IllegalArgumentException("Need to add at least one push provider.");
            }

            return new Configuration(
                    providersMap.values(),
                    eventListener,
                    isSelectSystemPreferred,
                    checkManifestHandler
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
