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

import org.onepf.opfpush.model.Operation;
import org.onepf.opfutils.OPFLog;

import static org.onepf.opfpush.model.Operation.REGISTER;

/**
 * @author Roman Savin
 * @since 04.02.2015
 */
final class RegisterBackoffAdapter<T extends Backoff> implements BackoffManager {

    @NonNull
    private Backoff registerBackoff;

    public RegisterBackoffAdapter(@NonNull final Class<T> backoffClass) {
        this.registerBackoff = createBackoff(backoffClass);
    }

    @Override
    public boolean hasTries(@NonNull final String providerName, @NonNull final Operation operation) {
        OPFLog.methodD(getClass(), "hasTries", providerName, operation);
        checkOperation(operation);
        return registerBackoff.hasTries();
    }

    @Override
    public long getTryDelay(@NonNull final String providerName, @NonNull final Operation operation) {
        OPFLog.methodD(getClass(), "getTryDelay", providerName, operation);
        checkOperation(operation);
        return registerBackoff.getTryDelay();
    }

    @Override
    public void reset(@NonNull final String providerName, @NonNull final Operation operation) {
        OPFLog.methodD(getClass(), "reset", providerName, operation);
        checkOperation(operation);
        registerBackoff.reset();
    }

    @NonNull
    private Backoff createBackoff(@NonNull final Class<T> backoffClass) {
        try {
            return backoffClass.newInstance();
        } catch (Exception e) {
            OPFLog.w("Exception while instantiating class " + backoffClass + " : " + e);
            OPFLog.w("Use " + InfinityExponentialBackoff.class.getSimpleName());
            return new InfinityExponentialBackoff();
        }
    }

    private void checkOperation(@NonNull final Operation operation) {
        if (operation != REGISTER) {
            throw new IllegalStateException("Wrong operation for RegisterBackoffAdapter : " + operation);
        }
    }
}
