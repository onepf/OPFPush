package org.onepf.openpush;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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
        ExponentialBackoff mBackoff = new ExponentialBackoff(6);
        assertEquals(6, mBackoff.getTryCount());
        for (int tryNumber = 1, expectedDelay = 2000;
             tryNumber <= mBackoff.getTryCount();
             tryNumber++, expectedDelay *= 2) {
            assertEquals(expectedDelay, mBackoff.getDelay(tryNumber));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDelayForZeroTryNumber() throws Exception {
        ExponentialBackoff mBackoff = new ExponentialBackoff(6);
        mBackoff.getDelay(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDelayForTryNumberBiggerThanTryCount() throws Exception {
        ExponentialBackoff mBackoff = new ExponentialBackoff(6);
        assertEquals(6, mBackoff.getTryCount());
        mBackoff.getDelay(7);
    }
}
