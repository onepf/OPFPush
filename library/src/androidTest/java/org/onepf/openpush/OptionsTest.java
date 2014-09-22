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
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * Created by krozov on 09.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OptionsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderTwiceAddProvider() {
        Options.Builder builder = new Options.Builder();
        PushProvider provider = new MockPushProvider(Robolectric.application);
        builder.addProviders(provider);
        builder.addProviders(provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithoutProvider() {
        new Options.Builder().build();
    }

    @Test
    public void testOptionsBuild() {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));

        Options options = builder.build();
        Assert.assertEquals(1, options.getProviders().size());
        Assert.assertEquals(MockPushProvider.class, options.getProviders().get(0).getClass());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableProviders() {
        Options.Builder builder = new Options.Builder();
        ArrayList<PushProvider> providers = new ArrayList<PushProvider>(1);
        final MockPushProvider mockPushProvider = new MockPushProvider(Robolectric.application);
        providers.add(mockPushProvider);
        builder.addProviders(providers);

        Options options = builder.build();
        Assert.assertNotNull(options.getProviders());
        Assert.assertNotSame(providers, options.getProviders());
        Assert.assertEquals(1, options.getProviders().size());
        Assert.assertSame(mockPushProvider, options.getProviders().get(0));

        options.getProviders().add(null);
    }
}
