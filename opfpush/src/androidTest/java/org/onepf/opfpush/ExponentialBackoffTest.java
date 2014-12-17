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

package org.onepf.opfpush;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.configuration.Backoff;
import org.onepf.opfpush.configuration.ExponentialBackoff;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 02.10.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ExponentialBackoffTest {

    @Test
    public void testDelay() throws Exception {
        final int backoffTryCount = 6;
        final Backoff backoff = new ExponentialBackoff(backoffTryCount);

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
        final ExponentialBackoff backoff = new ExponentialBackoff(tryCount);
        for (int i = 0; i <= tryCount; i++) {
            backoff.hasTries();
            backoff.getTryDelay();
        }
    }

    @Test
    public void testSummaryDelay() {
        final int backoffTryCount = 3;
        final ExponentialBackoff exponentialBackoff = new ExponentialBackoff(backoffTryCount);

        int summaryDelay = 0;
        for (int expectedDelay = 2000; exponentialBackoff.hasTries(); expectedDelay *= 2) {
            summaryDelay += expectedDelay;
            exponentialBackoff.getTryDelay();
        }

        Assert.assertEquals(summaryDelay, exponentialBackoff.summaryDelay());
    }
}
