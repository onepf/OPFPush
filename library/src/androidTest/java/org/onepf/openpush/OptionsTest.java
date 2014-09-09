package org.onepf.openpush;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by krozov on 09.09.14.
 */
public class OptionsTest {

    @Config(emulateSdk = 18, manifest = Config.NONE)
    @RunWith(RobolectricTestRunner.class)
    public static class BuilderTest {

        @Test(expected = IllegalArgumentException.class)
        public void testTwiceAddProvider() {
            Options.Builder builder = new Options.Builder();
            PushProvider provider = new StubPushProvider();
            builder.addProviders(provider);
            builder.addProviders(provider);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testWithoutProvider() {
            new Options.Builder().build();
        }

        @Test
        public void testBuilder() {
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
    }

    private static class StubPushProvider implements PushProvider {

        @Override
        public void register() {
        }

        @Override
        public void unregister() {
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public boolean isRegistered() {
            return false;
        }

        @Nullable
        @Override
        public String getRegistrationId() {
            return null;
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
    }

}
