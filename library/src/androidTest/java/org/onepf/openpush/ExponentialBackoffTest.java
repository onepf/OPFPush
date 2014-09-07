package org.onepf.openpush;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

/**
 * Created by krozov on 07.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ExponentialBackoffTest {
    @Test
    public void testPause() {
        ExponentialBackoff backoff = new ExponentialBackoff(5);
        Assert.assertEquals(TimeUnit.SECONDS.toMillis(2), backoff.getDelay(1));
        Assert.assertEquals(TimeUnit.SECONDS.toMillis(4), backoff.getDelay(2));
        Assert.assertEquals(TimeUnit.SECONDS.toMillis(8), backoff.getDelay(3));
        Assert.assertEquals(TimeUnit.SECONDS.toMillis(16), backoff.getDelay(4));
        Assert.assertEquals(TimeUnit.SECONDS.toMillis(32), backoff.getDelay(5));
    }
}
