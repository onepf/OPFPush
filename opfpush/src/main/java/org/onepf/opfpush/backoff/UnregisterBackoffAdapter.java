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

import java.util.HashMap;
import java.util.Map;

import static org.onepf.opfpush.model.Operation.UNREGISTER;

/**
 * @author Roman Savin
 * @since 03.02.2015
 */
final class UnregisterBackoffAdapter<T extends Backoff> implements BackoffManager {

    @NonNull
    private Class<T> backoffClass;

    @NonNull
    private final Map<String, Backoff> backoffMap = new HashMap<>();

    public UnregisterBackoffAdapter(@NonNull final Class<T> backoffClass) {
        this.backoffClass = backoffClass;
    }

    @Override
    public boolean hasTries(@NonNull final String providerName,
                            @NonNull final Operation operation) {
        OPFPushLog.methodD(getClass(), "hasTries", providerName, operation);
        checkOperation(operation);
        if (backoffMap.containsKey(providerName)) {
            OPFPushLog.d("Backoff map contains key for provider " + providerName);
            return backoffMap.get(providerName).hasTries();
        }

        final Backoff newUnregisterBackoff = createBackoff();
        backoffMap.put(providerName, newUnregisterBackoff);
        return newUnregisterBackoff.hasTries();
    }

    @Override
    public long getTryDelay(@NonNull final String providerName,
                            @NonNull final Operation operation) {
        OPFPushLog.methodD(getClass(), "getTryDelay", providerName, operation);
        checkOperation(operation);
        if (backoffMap.containsKey(providerName)) {
            OPFPushLog.d("Backoff map contains key for provider " + providerName);
            return backoffMap.get(providerName).getTryDelay();
        }

        final Backoff newUnregisterBackoff = createBackoff();
        backoffMap.put(providerName, newUnregisterBackoff);
        return newUnregisterBackoff.getTryDelay();
    }

    @Override
    public void reset(@NonNull final String providerName, @NonNull final Operation operation) {
        OPFPushLog.methodD(getClass(), "reset", providerName, operation);
        checkOperation(operation);
        if (backoffMap.containsKey(providerName)) {
            OPFPushLog.d("Backoff map contains key for provider " + providerName);
            backoffMap.remove(providerName);
        }
    }

    @NonNull
    private Backoff createBackoff() {
        try {
            return backoffClass.newInstance();
        } catch (Exception e) {
            OPFPushLog.w("Exception while instantiating class " + backoffClass + " : " + e);
            OPFPushLog.w("Use " + InfinityExponentialBackoff.class.getSimpleName());
            return new InfinityExponentialBackoff();
        }
    }

    private void checkOperation(@NonNull final Operation operation) {
        if (operation != UNREGISTER) {
            throw new IllegalStateException("Wrong operation for UnregisterBackoffAdapter : " + operation);
        }
    }
}
