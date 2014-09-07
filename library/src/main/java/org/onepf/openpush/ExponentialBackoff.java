package org.onepf.openpush;

/**
 * Created by krozov on 05.09.14.
 */
public final class ExponentialBackoff implements Backoff {
    private final int mTryCount;

    public ExponentialBackoff(int tryCount) {
        if (tryCount < 1) {
            throw new IllegalArgumentException("Try count can't less than 1.");
        }
        mTryCount = tryCount;
    }

    public ExponentialBackoff() {
        mTryCount = 4;
    }

    @Override
    public int tryCount() {
        return mTryCount;
    }

    @Override
    public long getDelay(int tryNumber) {
        return (2 << (tryNumber - 1)) * 1000L;
    }
}
