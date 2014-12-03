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
import org.onepf.opfpush.listener.TestEventListener;
import org.onepf.opfpush.mock.MockPushProvider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 11.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OptionsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderTwiceAddProvider() {
        final Options.Builder builder = new Options.Builder();
        final PushProvider provider = new MockPushProvider();
        builder.addProviders(provider);
        builder.addProviders(provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderAddProviderWithSameName() {
        final Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider());
        builder.addProviders(new MockPushProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithoutEventListener() {
        new Options.Builder()
                .addProviders(new MockPushProvider())
                .build();
    }

    @Test
    public void testBuilderProviderOrder() {
        final Options.Builder builder = new Options.Builder();
        final PushProvider[] providers = {
                new MockPushProvider("provider1"),
                new MockPushProvider("provider2"),
                new MockPushProvider("provider3"),
                new MockPushProvider("provider4")
        };
        builder.addProviders(providers);
        builder.setEventListener(new TestEventListener());
        final Options options = builder.build();

        final List<PushProvider> optionsProviders = options.getProviders();
        Assert.assertEquals(providers.length, optionsProviders.size());
        for (int i = 0; i < providers.length; i++) {
            final PushProvider provider = optionsProviders.get(i);
            Assert.assertNotNull(provider);
            Assert.assertEquals(providers[i].getName(), provider.getName());
            Assert.assertEquals(providers[i], provider);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithoutProvider() {
        new Options.Builder().build();
    }

    @Test
    public void testOptionsBuild() {
        final Options.Builder builder = new Options.Builder();
        final Options options = builder
                .addProviders(new MockPushProvider())
                .setEventListener(new TestEventListener())
                .build();

        Assert.assertEquals(1, options.getProviders().size());
        Assert.assertEquals(MockPushProvider.class, options.getProviders().get(0).getClass());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableProviders() {
        final Options.Builder builder = new Options.Builder();
        final List<PushProvider> providers = new ArrayList<PushProvider>(1);
        final MockPushProvider mockPushProvider = new MockPushProvider("provider1");
        providers.add(mockPushProvider);
        builder.addProviders(providers);
        builder.setEventListener(new TestEventListener());

        final Options options = builder.build();
        Assert.assertNotNull(options.getProviders());
        Assert.assertNotSame(providers, options.getProviders());
        Assert.assertEquals(1, options.getProviders().size());
        Assert.assertSame(mockPushProvider, options.getProviders().get(0));

        options.getProviders().add(new MockPushProvider("provider2"));
    }
}
