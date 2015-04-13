package org.onepf.opfpush.configuration;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.mock.MockNamePushProvider;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfpush.testutil.Util;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * @author antonpp
 * @since 13.04.15
 */
@Config(emulateSdk = JELLY_BEAN_MR2, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ConfigurationBuilderTest extends Assert {

    private Configuration.Builder builder;

    @Before
    public void setUp() {
        builder = new Configuration.Builder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProvidersEmpty() {
        builder.addProviders();
        builder.addProviders(new ArrayList<PushProvider>());
        builder.build();
    }

    @Test
    public void testAddProviders() {
        final PushProvider[] providers = Util.getRandomPushProviders();
        final Set<PushProvider> uniqueProviders = new HashSet<>(Arrays.asList(providers));
        builder.addProviders(uniqueProviders.toArray(new PushProvider[uniqueProviders.size()]));
        final Configuration configuration = builder.build();
        final List<PushProvider> resultProviders = configuration.getProviders();
        assertTrue(resultProviders.containsAll(uniqueProviders));
        assertTrue(uniqueProviders.containsAll(resultProviders));
        assertEquals(resultProviders.size(), uniqueProviders.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProvidersNotUnique() {
        final MockNamePushProvider pushProvider = new MockNamePushProvider("provider");
        final PushProvider[] providers = {pushProvider, pushProvider};
        builder.addProviders(providers);
        builder.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProvidersNotUniqueTwoCalls() {
        final MockNamePushProvider pushProvider = new MockNamePushProvider("provider");
        builder.addProviders(pushProvider).addProviders(pushProvider);
        builder.build();
    }

    @Test
    public void testToString() {
        final PushProvider[] providers = Util.getRandomPushProviders();
        final Set<PushProvider> uniqueProviders = new HashSet<>(Arrays.asList(providers));
        builder.addProviders(uniqueProviders.toArray(new PushProvider[uniqueProviders.size()]));
        assertNotNull(builder.toString());
        final Configuration configuration = builder.build();
        assertNotNull(configuration.toString());
    }

    @Test
    public void testSetEventListener() {
        final PushProvider[] providers = Util.getRandomPushProviders();
        final Set<PushProvider> uniqueProviders = new HashSet<>(Arrays.asList(providers));
        final EventListener eventListener = new MockEventListener();
        builder.addProviders(uniqueProviders.toArray(new PushProvider[uniqueProviders.size()])).setEventListener(eventListener);
        final Configuration configuration = builder.build();
        assertEquals(configuration.getEventListener(), eventListener);
    }

    @Test
    public void testSetSelectSystemPreferred() {
        final PushProvider[] providers = Util.getRandomPushProviders();
        final Set<PushProvider> uniqueProviders = new HashSet<>(Arrays.asList(providers));
        builder.addProviders(uniqueProviders.toArray(new PushProvider[uniqueProviders.size()])).setSelectSystemPreferred(true);
        Configuration configuration = builder.build();
        assertTrue(configuration.isSelectSystemPreferred());
        builder = new Configuration.Builder();
        builder.addProviders(uniqueProviders.toArray(new PushProvider[uniqueProviders.size()])).setSelectSystemPreferred(false);
        configuration = builder.build();
        assertFalse(configuration.isSelectSystemPreferred());
    }

    private final class MockEventListener implements EventListener {

        @Override
        public void onMessage(@NonNull Context context, @NonNull String providerName, @Nullable Bundle extras) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onDeletedMessages(@NonNull Context context, @NonNull String providerName, int messagesCount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onRegistered(@NonNull Context context, @NonNull String providerName, @NonNull String registrationId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onUnregistered(@NonNull Context context, @NonNull String providerName, @Nullable String registrationId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onNoAvailableProvider(@NonNull Context context, @NonNull Map<String, UnrecoverablePushError> pushErrors) {
            throw new UnsupportedOperationException();
        }
    }
}
