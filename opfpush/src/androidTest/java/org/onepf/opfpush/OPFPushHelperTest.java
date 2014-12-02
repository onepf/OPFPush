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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
 * @author Kirill Rozov
 * @see OPFPushHelper
 * @since 11.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OPFPushHelperTest {

    static {
        // redirect the Log.x output to stdout. Stdout will be recorded in the test result report
        ShadowLog.stream = System.out;
    }

    public static final String PACKAGE_CHANGE_RECEIVER_CLASS_NAME = "PackageChangeReceiver";

    private static void checkProviderRegistrationState(@NonNull OPFPushHelper helper,
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

    private static Options getOptions(@NonNull OPFPushHelper helper) {
        try {
            Field mOptionsField = OPFPushHelper.class.getDeclaredField("mOptions");
            if (!mOptionsField.isAccessible()) {
                mOptionsField.setAccessible(true);
            }
            return (Options) mOptionsField.get(helper);
        } catch (Exception e) {
            throw new RuntimeException("Error get options");
        }
    }

    private static OPFPushHelper createHelperWithInfinityRegisterProvider() {
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockInfinityRegisterPushProvider());
        helper.init(builder.build());
        return helper;
    }

    private static OPFPushHelper createHelperWithInfinityUnregisterProvider() {
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockInfinityUnregisterPushProvider());
        helper.init(builder.build());
        return helper;
    }

    private static MockPushProvider createHelperAndRegisterWithMockProvider() {
        Options.Builder builder = new Options.Builder();
        MockPushProvider provider
                = new MockPushProvider("providerForPref");
        builder.addProviders(provider);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        helper.register();
        return provider;
    }

    private static void restoreUnavailableProvider_RecoverDisable(PushProvider... providers) {
        Options.Builder builder = new Options.Builder();
        builder.setRecoverProvider(false);
        builder.addProviders(providers);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());

        assertNull(helper.getCurrentProvider());

        checkProviderRegistrationState(helper, null);
    }

    private static OPFPushHelper createHelperWithMockSenderPushProvider() {
        Options.Builder builder = new Options.Builder();
        PushProvider provider
                = new MockSenderProvider();
        builder.addProviders(provider);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        return helper;
    }

    @Before
    public void setup() {
        Robolectric.packageManager.addPackage(MockPushProvider.DEFAULT_HOST_APP_PACKAGE);
    }

    @Test
    public void testInit() throws Exception {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider());
        OPFPushHelper opfPushHelper = OPFPushHelper.newInstance(Robolectric.application);
        assertFalse(opfPushHelper.isInitDone());
        opfPushHelper.init(builder.build());
        assertTrue(opfPushHelper.isInitDone());
    }

    @Test(expected = OPFPushException.class)
    public void testInitTwice() throws Exception {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider());
        OPFPushHelper opfPushHelper = OPFPushHelper.newInstance(Robolectric.application);
        assertFalse(opfPushHelper.isInitDone());
        opfPushHelper.init(builder.build());
        assertTrue(opfPushHelper.isInitDone());
        opfPushHelper.init(builder.build());
    }

    @Test(expected = OPFPushException.class)
    public void testUnregisterWithoutRegister() throws Exception {
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider());
        Options options = builder.build();
        helper.init(options);

        helper.unregister();
    }

    @Test
    public void testRegister() throws Exception {
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        final String providerName = "providerName";
        builder.addProviders(new MockPushProvider(providerName));
        Options options = builder.build();
        helper.init(options);
        assertTrue(helper.isInitDone());

        helper.register();
        PushProvider provider = helper.getCurrentProvider();
        assertNotNull(provider);
        assertTrue(provider.isRegistered());
        assertEquals(providerName, provider.getName());
        assertNotNull(provider.getRegistrationId());

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

    @Test(expected = OPFPushException.class)
    public void testUnregisterBeforeInit() throws Exception {
        OPFPushHelper.newInstance(Robolectric.application).unregister();
    }

    @Test(expected = OPFPushException.class)
    public void testRegisterBeforeInit() throws Exception {
        OPFPushHelper.newInstance(Robolectric.application).register();
    }

    @Test
    public void testRegisterSecondProvider() throws Exception {
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        Options.Builder builder = new Options.Builder();
        builder.addProviders(
                new MockPushProvider("providerName1", false),
                new MockPushProvider("providerName2")
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
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        Options.Builder builder = new Options.Builder();
        final PushProvider[] providers = {
                new MockPushProvider("provider1", "org.openpf.store1"),
                new MockPushProvider("provider2", "org.openpf.store2")
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
        helper.onProviderUnavailable(currentProvider);

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
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        final String providerName = "providerName";
        builder.addProviders(new MockPushProvider(providerName));
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

    @Test(expected = OPFPushException.class)
    public void testRegisterTwice() throws Exception {
        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider());
        Options options = builder.build();
        helper.init(options);

        helper.register();
        assertTrue(helper.isRegistered());
        helper.register();
    }

    @Test(expected = OPFPushException.class)
    public void testUnregisterWhileRegistrationRun() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityRegisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
        helper.unregister();
    }

    @Test
    public void testRegisterWhileRegistrationRun() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityRegisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
    }

    @Test(expected = OPFPushException.class)
    public void testRegisterWhileUnregistrationRun() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertTrue(helper.isRegistered());
        helper.unregister();
        assertTrue(helper.isRegistered());
        helper.register();
    }

    @Test
    public void testRestoreLastProvider() throws Exception {
        MockPushProvider provider = createHelperAndRegisterWithMockProvider();

        Settings settings = new Settings(Robolectric.application);
        final String lastProviderName = settings.getLastProviderName();
        assertNotNull(lastProviderName);
        assertEquals(provider.getName(), lastProviderName);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(new Options.Builder().addProviders(provider).build());
        assertSame(provider, helper.getCurrentProvider());
        assertTrue(helper.isRegistered());
    }

    @Test
    public void testUnregisterWhileUnregistrationRun() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertTrue(helper.isRegistered());
        helper.unregister();
        assertTrue(helper.isRegistered());
        helper.unregister();
        assertTrue(helper.isRegistered());
    }

    @Test
    public void testRestoreUnavailableProvider() throws Exception {
        final MockPushProvider provider = createHelperAndRegisterWithMockProvider();

        Settings settings = new Settings(Robolectric.application);
        String lastProviderName = settings.getLastProviderName();
        assertNotNull(lastProviderName);
        assertFalse(TextUtils.isEmpty(lastProviderName));
        assertEquals(provider.getName(), lastProviderName);

        provider.setCheckAvailability(false);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        Options options = new Options.Builder().addProviders(provider).build();
        helper.init(options);
        assertNull(helper.getCurrentProvider());
        assertFalse(helper.isRegistered());
        assertNull(settings.getLastProviderName());
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverEnable() throws Exception {
        MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setCheckAvailability(false);

        PushProvider nextProvider = new MockPushProvider();

        Options.Builder builder = new Options.Builder();
        builder.setRecoverProvider(true);
        builder.addProviders(lastProvider);
        builder.addProviders(nextProvider);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        assertTrue(helper.isRegistered());

        final PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);
        checkProviderRegistrationState(helper, currentProvider);
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverDisable() throws Exception {
        MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setCheckAvailability(false);

        restoreUnavailableProvider_RecoverDisable(
                lastProvider, new MockPushProvider());
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverDisable2() throws Exception {
        MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setCheckAvailability(false);
        restoreUnavailableProvider_RecoverDisable(
                new MockPushProvider(), lastProvider);
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverEnable_AllProvidersUnavailable()
            throws Exception {

        MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setCheckAvailability(false);

        Options.Builder builder = new Options.Builder();
        builder.setRecoverProvider(true);
        PushProvider[] providers = {
                new MockPushProvider(false),
                lastProvider
        };
        builder.addProviders(providers);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());

        checkProviderRegistrationState(helper, null);
    }

    @Test
    public void testSendRegistrationResult_RegistrationRunning() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityRegisterProvider();
        helper.register();
        helper.getReceivedMessageHandler().onRegistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test(expected = OPFPushException.class)
    public void testSendResultWithNonExistenProvider_RegistrationRunning() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityRegisterProvider();
        helper.register();
        helper.getReceivedMessageHandler().onRegistered("123123123", "testId");
    }

    @Test(expected = IllegalStateException.class)
    public void testSendUnregistrationResult_RegistrationRunning() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityRegisterProvider();
        helper.register();
        helper.getReceivedMessageHandler().onUnregistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test(expected = IllegalStateException.class)
    public void testSendRegistrationResult_UnregistrationRunning() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();
        helper.register();
        helper.unregister();
        helper.getReceivedMessageHandler().onRegistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test
    public void testSendUnregistrationResult_UnregistrationRunning() throws Exception {
        OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();
        helper.register();
        helper.unregister();
        helper.getReceivedMessageHandler().onUnregistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test(expected = OPFPushException.class)
    public void testSendMessageWithNotSendSupportProvider() throws Exception {
        createHelperAndRegisterWithMockProvider();
        OPFPushHelper helper = OPFPushHelper.getInstance(Robolectric.application);
        assertFalse(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @Test
    public void testSendMessage() throws Exception {
        OPFPushHelper helper = createHelperWithMockSenderPushProvider();
        helper.register();
        assertTrue(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @Test(expected = OPFPushException.class)
    public void testSendMessageWhenNotRegistered() throws Exception {
        OPFPushHelper helper = createHelperWithMockSenderPushProvider();
        assertFalse(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @Test(expected = OPFPushException.class)
    public void testSendMessageWhenNotRegisteredWithNotSendSupportProvider() throws Exception {
        Options.Builder builder = new Options.Builder();
        MockPushProvider provider
                = new MockPushProvider("providerForPref");
        builder.addProviders(provider);

        OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        assertFalse(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @After
    public void tearDown() {
        new Settings(Robolectric.application).clear();
        Robolectric.packageManager.removePackage(MockPushProvider.DEFAULT_HOST_APP_PACKAGE);
    }

    private static class MockSenderProvider extends MockPushProvider implements SenderPushProvider {
        @Override
        public void send(@NonNull Message msg) {
        }
    }

    private static class MockInfinityRegisterPushProvider extends MockPushProvider {
        public MockInfinityRegisterPushProvider() {
            super();
        }

        @Override
        public void register() {
        }
    }

    private static class MockInfinityUnregisterPushProvider extends MockPushProvider {
        public MockInfinityUnregisterPushProvider() {
            super();
        }

        @Override
        public void unregister() {
        }
    }
}
