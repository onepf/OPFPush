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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by krozov on 07.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ExponentialBackoffTest {

    private ExponentialBackoff mBackoff;

    @Before
    public void setup() {
        mBackoff = new ExponentialBackoff(6);
    }

    @Test
    public void testDelay() {
        for (int tryNumber = 1, expectedDelay = 2000;
             tryNumber <= mBackoff.getTryCount();
             tryNumber++, expectedDelay *= 2) {
            Assert.assertEquals(expectedDelay, mBackoff.getDelay(tryNumber));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDelayForZeroTryNumber() {
        mBackoff.getDelay(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDelayForTryNumberBiggerThanTryCount() {
        mBackoff.getDelay(mBackoff.getTryCount() + 1);
    }
}
