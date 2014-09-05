package org.onepf.openpush.retrypolice;

/**
 * Created by krozov on 05.09.14.
 */
public final class ExponentialPolice implements RetryPolice {
    public ExponentialPolice() {
    }

    @Override
    public int tryCount() {
        return 4;
    }

    @Override
    public long getDelay(int tryNumber) {
        return (2 << tryNumber) * 1000L;
    }
}
