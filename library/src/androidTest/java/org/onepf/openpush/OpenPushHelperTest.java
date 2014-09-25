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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.openpush.mock.MockInfinityRegisterPushProvider;
import org.onepf.openpush.mock.MockInfinityUnregisterPushProvider;
import org.onepf.openpush.mock.MockPushProvider;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OpenPushHelperTest {

    static {
        // redirect the Log.x output to stdout. Stdout will be recorded in the test result report
        ShadowLog.stream = System.out;
    }

    public static final String PACKAGE_CHANGE_RECEIVER_CLASS_NAME = "PackageChangeReceiver";

    private static void checkProviderRegistrationState( @NonNull OpenPushHelper helper,
            @Nullable PushProvider expectedRegisteredProvider) {

        if (expectedRegisteredProvider != null) {
            assertTrue(helper.isRegistered());

            PushProvider currentProvider = helper.getCurrentProvider();
            assertNotNull(currentProvider);
            assertSame(expectedRegisteredProvider, currentProvider);
        } else {
            assertFalse(helper.isRegistered());
            assertNull(helper.getCurrentProvider());
        }

        for (PushProvider provider : getOptions(helper).getProviders()) {
            if (provider == expectedRegisteredProvider) {
                assertTrue(provider.isRegistered());
                assertNotNull(provider.getRegistrationId());
            } else {
                assertFalse(provider.isRegistered());
                assertNull(provider.getRegistrationId());
            }
        }
    }

    private static Options getOptions(@NonNull OpenPushHelper helper) {
        try {
            Field mOptionsField = OpenPushHelper.class.getDeclaredField("mOptions");
            if (!mOptionsField.isAccessible()) {
                mOptionsField.setAccessible(true);
            }
            return (Options) mOptionsField.get(helper);
        } catch (Exception e) {
            throw new RuntimeException("Error get options");
        }
    }

    private static OpenPushHelper createHelperWithInfinityRegisterProvider() {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockInfinityRegisterPushProvider(Robolectric.application));
        helper.init(builder.build());
        return helper;
    }

    private static OpenPushHelper createHelperWithInfinityUnregisterProvider() {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockInfinityUnregisterPushProvider(Robolectric.application));
        helper.init(builder.build());
        return helper;
    }

    private static MockPushProvider initWithMockProvider() {
        Options.Builder builder = new Options.Builder();
        MockPushProvider provider
                = new MockPushProvider(Robolectric.application, "providerForPref");
        builder.addProviders(provider);

        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        helper.init(builder.build());
        helper.register();
        return provider;
    }

    private static void restoreUnavailableProvider_RecoverDisable(PushProvider... providers) {

        Options.Builder builder = new Options.Builder();
        builder.setRecoverProvider(false);
        builder.addProviders(providers);

        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        helper.init(builder.build());

        assertNull(helper.getCurrentProvider());

        checkProviderRegistrationState(helper, null);
    }

    @Before
    public void setup() {
        Robolectric.packageManager.addPackage(MockPushProvider.DEFAULT_HOST_APP_PACKAGE);
    }

    @Test
    public void testInit() throws Exception {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = OpenPushHelper.getNewInstance(Robolectric.application);
        assertFalse(openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
        assertTrue(openPushHelper.isInitDone());
    }

    @Test(expected = OpenPushException.class)
    public void testInitTwice() throws Exception {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = OpenPushHelper.getNewInstance(Robolectric.application);
        assertFalse(openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
        assertTrue(openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
    }

    @Test(expected = OpenPushException.class)
    public void testUnregisterWithoutRegister() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        Options options = builder.build();
        helper.init(options);

        helper.unregister();
    }

    @Test
    public void testRegister() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        final String providerName = "providerName";
        builder.addProviders(new MockPushProvider(Robolectric.application, providerName));
        Options options = builder.build();
        helper.init(options);
        assertTrue(helper.isInitDone());

        helper.register();
        PushProvider provider = helper.getCurrentProvider();
        assertNotNull(provider);
        assertTrue(provider.isRegistered());
        assertEquals(providerName, provider.getName());
        assertNotNull(provider.getRegistrationId());
        testPackageChangeReceiverRegistered();
    }

    public void testPackageChangeReceiverRegistered() throws Exception {
        List<ShadowApplication.Wrapper> registeredReceivers =
                Robolectric.getShadowApplication().getRegisteredReceivers();
        assertFalse(registeredReceivers.isEmpty());
        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (PACKAGE_CHANGE_RECEIVER_CLASS_NAME.equals(
                    wrapper.broadcastReceiver.getClass().getSimpleName())) {
                receiverFound = true;
                break;
            }
        }

        assertTrue(receiverFound); //will be false if not found
    }

    @Test(expected = OpenPushException.class)
    public void testUnregisterBeforeInit() throws Exception {
        OpenPushHelper.getNewInstance(Robolectric.application).unregister();
    }

    @Test(expected = OpenPushException.class)
    public void testRegisterBeforeInit() throws Exception {
        OpenPushHelper.getNewInstance(Robolectric.application).register();
    }

    @Test
    public void testRegisterSecondProvider() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        Options.Builder builder = new Options.Builder();
        builder.addProviders(
                new MockPushProvider(Robolectric.application, "providerName1", false),
                new MockPushProvider(Robolectric.application, "providerName2")
        );
        helper.init(builder.build());

        assertTrue(helper.isInitDone());

        checkProviderRegistrationState(helper, null);

        helper.register();
        PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);

        checkProviderRegistrationState(helper, currentProvider);
    }

    @Test
    public void testRemoveHostAppPackage() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        Options.Builder builder = new Options.Builder();
        final PushProvider[] providers = {
                new MockPushProvider(Robolectric.application, "provider1", "org.openpf.store1"),
                new MockPushProvider(Robolectric.application, "provider2", "org.openpf.store2")
        };
        builder.addProviders(providers);
        helper.init(builder.build());

        for (PushProvider provider : providers) {
            Robolectric.packageManager.addPackage(provider.getHostAppPackage());
            assertFalse(provider.isRegistered());
        }

        helper.register();

        PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);

        checkProviderRegistrationState(helper, currentProvider);

        Robolectric.packageManager.removePackage(currentProvider.getHostAppPackage());
        helper.onUnavailable(currentProvider);

        PushProvider oldCurrentProvider = currentProvider;
        currentProvider = helper.getCurrentProvider();

        assertNotNull(currentProvider);
        assertNotSame(oldCurrentProvider, currentProvider);

        checkProviderRegistrationState(helper, currentProvider);

        for (PushProvider provider : providers) {
            Robolectric.packageManager.removePackage(provider.getHostAppPackage());
        }
    }

    @Test
    public void testUnregister() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        final String providerName = "providerName";
        builder.addProviders(new MockPushProvider(Robolectric.application, providerName));
        Options options = builder.build();
        helper.init(options);
        assertTrue(helper.isInitDone());

        helper.register();

        PushProvider registeredProvider = helper.getCurrentProvider();
        assertNotNull(registeredProvider);

        helper.unregister();

        assertFalse(helper.isRegistered());
        assertFalse(registeredProvider.isRegistered());
        assertNull(helper.getCurrentProvider());
    }

    @Test(expected = OpenPushException.class)
    public void testRegisterTwice() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        Options options = builder.build();
        helper.init(options);

        helper.register();
        assertTrue(helper.isRegistered());
        helper.register();
    }

    @Test(expected = OpenPushException.class)
    public void testUnregisterWhileRegistrationRun() throws Exception {
        OpenPushHelper helper = createHelperWithInfinityRegisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
        helper.unregister();
    }

    @Test
    public void testRegisterWhileRegistrationRun() throws Exception {
        OpenPushHelper helper = createHelperWithInfinityRegisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
    }

    @Test(expected = OpenPushException.class)
    public void testRegisterWhileUnregistrationRun() throws Exception {
        OpenPushHelper helper = createHelperWithInfinityUnregisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertTrue(helper.isRegistered());
        helper.unregister();
        assertTrue(helper.isRegistered());
        helper.register();
    }

    @Test
    public void testRestoreLastProvider() throws Exception {
        MockPushProvider provider = initWithMockProvider();

        SharedPreferences prefs =
                Robolectric.application.getSharedPreferences(OpenPushHelper.PREF_NAME, Context.MODE_PRIVATE);
        String lastProviderName = prefs.getString(OpenPushHelper.KEY_LAST_PROVIDER_NAME, null);
        assertNotNull(lastProviderName);
        assertEquals(provider.getName(), lastProviderName);

        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        helper.init(new Options.Builder().addProviders(provider).build());
        assertSame(provider, helper.getCurrentProvider());
        assertTrue(helper.isRegistered());
    }

    @Test
    public void testUnregisterWhileUnregistrationRun() throws Exception {
        OpenPushHelper helper = createHelperWithInfinityUnregisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertTrue(helper.isRegistered());
        helper.unregister();
        assertTrue(helper.isRegistered());
        helper.unregister();
        assertTrue(helper.isRegistered());
    }

    @Test
    public void testRestoreUnavailableProvider() {
        MockPushProvider provider = initWithMockProvider();
        provider.setAvailable(false);

        SharedPreferences prefs =
                Robolectric.application.getSharedPreferences(OpenPushHelper.PREF_NAME, Context.MODE_PRIVATE);
        assertTrue(prefs.contains(OpenPushHelper.KEY_LAST_PROVIDER_NAME));
        String lastProviderName = prefs.getString(OpenPushHelper.KEY_LAST_PROVIDER_NAME, null);
        assertFalse(TextUtils.isEmpty(lastProviderName));
        assertEquals(provider.getName(), lastProviderName);

        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        helper.init(new Options.Builder().addProviders(provider).build());
        assertNull(helper.getCurrentProvider());
        assertFalse(helper.isRegistered());
        assertFalse(prefs.contains(OpenPushHelper.KEY_LAST_PROVIDER_NAME));
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverEnable() {
        MockPushProvider lastProvider = initWithMockProvider();
        lastProvider.setAvailable(false);

        PushProvider nextProvider = new MockPushProvider(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        builder.setRecoverProvider(true);
        builder.addProviders(lastProvider);
        builder.addProviders(nextProvider);

        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        helper.init(builder.build());
        assertTrue(helper.isRegistered());

        final PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);
        checkProviderRegistrationState(
                helper, currentProvider
        );
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverDisable() {
        MockPushProvider lastProvider = initWithMockProvider();
        lastProvider.setAvailable(false);

        restoreUnavailableProvider_RecoverDisable(
                lastProvider, new MockPushProvider(Robolectric.application));
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverDisable2() {
        MockPushProvider lastProvider = initWithMockProvider();
        lastProvider.setAvailable(false);
        restoreUnavailableProvider_RecoverDisable(
                new MockPushProvider(Robolectric.application), lastProvider);
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverEnable_AllProvidersUnavailable() {
        MockPushProvider lastProvider = initWithMockProvider();
        lastProvider.setAvailable(false);

        Options.Builder builder = new Options.Builder();
        builder.setRecoverProvider(true);
        PushProvider[] providers = {
                new MockPushProvider(Robolectric.application, false),
                lastProvider
        };
        builder.addProviders(providers);

        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        helper.init(builder.build());

        checkProviderRegistrationState(helper, null);
    }

    @After
    public void tearDown() {
        Robolectric.application.getSharedPreferences(OpenPushHelper.PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear();
        Robolectric.packageManager.removePackage(MockPushProvider.DEFAULT_HOST_APP_PACKAGE);
    }
}
