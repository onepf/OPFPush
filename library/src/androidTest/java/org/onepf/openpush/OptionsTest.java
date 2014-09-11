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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        PushProvider provider = new StubPushProvider();
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
        builder.addProviders(new StubPushProvider());
        builder.setBackoff(new ExponentialBackoff());

        Options options = builder.build();
        Assert.assertEquals(1, options.getProviders().size());
        Assert.assertEquals(StubPushProvider.class, options.getProviders().get(0).getClass());

        final Backoff backoff = options.getBackoff();
        Assert.assertNotNull(backoff);
        Assert.assertEquals(ExponentialBackoff.class, backoff.getClass());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableProviders() {
        Options.Builder builder = new Options.Builder();
        ArrayList<PushProvider> providers = new ArrayList<PushProvider>(1);
        final StubPushProvider stubPushProvider = new StubPushProvider();
        providers.add(stubPushProvider);
        builder.addProviders(providers);

        Options options = builder.build();
        Assert.assertNull(options.getBackoff());
        Assert.assertNotNull(options.getProviders());
        Assert.assertNotSame(providers, options.getProviders());
        Assert.assertEquals(1, options.getProviders().size());
        Assert.assertSame(stubPushProvider, options.getProviders().get(0));

        options.getProviders().add(null);
    }

    private static class StubPushProvider extends BasePushProvider {

        private boolean mRegistered;

        private StubPushProvider() {
            super(Robolectric.application, , );
        }

        @Override
        public void register() {
            mRegistered = true;
        }

        @Override
        public void unregister() {
            mRegistered = false;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public boolean isRegistered() {
            return mRegistered;
        }

        @Nullable
        @Override
        public String getRegistrationId() {
            return "stub registration id";
        }

        @NotNull
        @Override
        public String getName() {
            return "StubPushProvider";
        }

        @NotNull
        @Override
        public String getHostAppPackage() {
            return "org.onepf.openpush.sample";
        }

        @Override
        public void close() {
        }

        @Override
        public void onAppStateChanged() {
        }

        @Override
        public boolean checkManifest() {
            return true;
        }
    }
}
