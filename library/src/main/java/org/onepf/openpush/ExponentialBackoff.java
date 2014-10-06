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

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kirill Rozov
 * @since 05.09.14.
 */
public final class ExponentialBackoff implements Backoff {
    public static final int DEFAULT_TRY_COUNT = 4;
    private final int mTryCount;

    @NonNull
    private final AtomicInteger mTryNumber = new AtomicInteger(0);

    public ExponentialBackoff(int tryCount) {
        if (tryCount < 1) {
            throw new IllegalArgumentException("Try count can't less than 1.");
        }
        mTryCount = tryCount;
    }

    public ExponentialBackoff() {
        mTryCount = DEFAULT_TRY_COUNT;
    }

    /**
     * Compute summary delay for all tries.
     */
    public int summaryDelay() {
        int summaryDelay = 0;
        for (int tryNumber = 1; tryNumber <= mTryCount; tryNumber++) {
            summaryDelay += getTryDelay(tryNumber);
        }
        return summaryDelay;
    }

    @Override
    public long getTryDelay() {
        if (mTryNumber.get() > mTryCount) {
            throw new NoSuchElementException();
        }
        return getTryDelay(mTryNumber.get());
    }

    private long getTryDelay(int tryNumber) {
        return TimeUnit.SECONDS.toMillis((int) Math.pow(2, tryNumber));
    }

    @Override
    public void reset() {
        mTryNumber.set(0);
    }

    @Override
    public boolean hasTries() {
        return mTryNumber.getAndIncrement() < mTryCount;
    }
}