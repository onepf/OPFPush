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

package org.onepf.opfpush.backoff;

import android.support.annotation.NonNull;

import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.model.Operation;

/**
 * @author Roman Savin
 * @since 04.02.2015
 */
public final class InfinityExponentialBackoffManager implements BackoffManager {

    private static InfinityExponentialBackoffManager instance = null;

    @NonNull
    private final BackoffManager registerBackoffAdapter;

    @NonNull
    private final BackoffManager unregisterBackoffAdapter;

    private InfinityExponentialBackoffManager() {
        registerBackoffAdapter = new RegisterBackoffAdapter<>(InfinityExponentialBackoff.class);
        unregisterBackoffAdapter = new UnregisterBackoffAdapter<>(InfinityExponentialBackoff.class);
    }

    public static InfinityExponentialBackoffManager getInstance() {
        if (instance == null) {
            instance = new InfinityExponentialBackoffManager();
        }

        return instance;
    }

    public boolean hasTries(@NonNull final String providerName,
                            @NonNull final Operation operation) {
        OPFPushLog.methodD(getClass(), "hasTries", providerName, operation);
        return getManagerByOperation(operation).hasTries(providerName, operation);
    }

    public long getTryDelay(@NonNull final String providerName,
                            @NonNull final Operation operation) {
        OPFPushLog.methodD(getClass(), "getTryDelay", providerName, operation);
        return getManagerByOperation(operation).getTryDelay(providerName, operation);
    }

    public void reset(@NonNull final String providerName,
                      @NonNull final Operation operation) {
        OPFPushLog.methodD(getClass(), "reset", providerName, operation);
        getManagerByOperation(operation).reset(providerName, operation);
    }

    @NonNull
    private BackoffManager getManagerByOperation(@NonNull final Operation operation) {
        switch (operation) {
            case REGISTER:
                return registerBackoffAdapter;
            case UNREGISTER:
                return unregisterBackoffAdapter;
        }
        throw new IllegalStateException("Wrong operation : " + operation);
    }
}
