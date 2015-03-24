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

import org.onepf.opfutils.OPFLog;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 05.09.14.
 */
@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
final class InfinityExponentialBackoff implements Backoff {

    public static final int MAX_TRY_COUNT = 16;

    @NonNull
    private final AtomicInteger tryNumber = new AtomicInteger(0);

    /**
     * Compute summary delay for all tries.
     */
    @SuppressWarnings("UnusedDeclaration")
    public int summaryDelay() {
        return Integer.MAX_VALUE;
    }

    @Override
    public synchronized long getTryDelay() {
        int currentTryNumber = 0;
        if (!tryNumber.compareAndSet(MAX_TRY_COUNT, 0)) {
            currentTryNumber = tryNumber.getAndIncrement();
        }
        return getTryDelay(currentTryNumber);
    }

    private long getTryDelay(int currentTryNumber) {
        OPFLog.logMethod(currentTryNumber);
        return TimeUnit.SECONDS.toMillis(2 << currentTryNumber);
    }

    @Override
    public void reset() {
        tryNumber.set(0);
    }

    @Override
    public boolean hasTries() {
        return true;
    }
}
