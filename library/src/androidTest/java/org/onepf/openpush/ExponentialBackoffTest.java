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
