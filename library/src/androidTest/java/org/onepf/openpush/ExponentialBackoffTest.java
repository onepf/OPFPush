package org.onepf.openpush;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

/**
 * @author Kirill Rozov
 * @since 02.10.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ExponentialBackoffTest {

    @Test
    public void testDelay() throws Exception {
        final int backoffTryCount = 6;
        ExponentialBackoff backoff = new ExponentialBackoff(backoffTryCount);
        int tryCount = 0;
        for (int expectedDelay = 2000; backoff.hasTries(); expectedDelay *= 2) {
            tryCount++;
            assertEquals(expectedDelay, backoff.getTryDelay());
        }
        Assert.assertEquals(backoffTryCount, tryCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithZeroTryCount() throws Exception {
        new ExponentialBackoff(0);
    }

    @Test(expected = NoSuchElementException.class)
    public void test() throws Exception {
        int tryCount = 1;
        ExponentialBackoff backoff = new ExponentialBackoff(tryCount);
        for (int i = 0; i <= tryCount; i++) {
            backoff.hasTries();
            backoff.getTryDelay();
        }
    }
}
