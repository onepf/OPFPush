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
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.listener.TestEventListener;
import org.onepf.opfpush.mock.MockInfinityRegisterPushProvider;
import org.onepf.opfpush.mock.MockInfinityUnregisterPushProvider;
import org.onepf.opfpush.mock.MockPushProvider;
import org.onepf.opfpush.mock.MockSenderPushProvider;
import org.onepf.opfpush.model.Message;
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
 * @author Roman Savin
 * @see OPFPushHelper
 * @since 11.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OPFPushHelperTest {

    public static final String PACKAGE_CHANGE_RECEIVER_CLASS_NAME = "PackageChangeReceiver";

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        Robolectric.packageManager.addPackage(MockPushProvider.DEFAULT_HOST_APP_PACKAGE);
    }

    @Test
    public void testInit() throws Exception {
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockPushProvider())
                .setEventListener(new TestEventListener());
        final OPFPushHelper opfPushHelper = OPFPushHelper.newInstance(Robolectric.application);

        assertFalse(opfPushHelper.isInitDone());

        opfPushHelper.init(builder.build());
        assertTrue(opfPushHelper.isInitDone());
    }

    @Test(expected = OPFPushException.class)
    public void testInitTwice() throws Exception {
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockPushProvider())
                .setEventListener(new TestEventListener());
        final OPFPushHelper opfPushHelper = OPFPushHelper.newInstance(Robolectric.application);

        assertFalse(opfPushHelper.isInitDone());
        opfPushHelper.init(builder.build());
        assertTrue(opfPushHelper.isInitDone());
        opfPushHelper.init(builder.build());
    }

    @Test(expected = OPFPushException.class)
    public void testUnregisterWithoutRegister() throws Exception {
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockPushProvider())
                .setEventListener(new TestEventListener());
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());

        helper.unregister();
    }

    @Test
    public void testRegister() throws Exception {
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        final String providerName = "providerName";

        final PushProvider provider = new MockPushProvider(providerName);
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(provider)
                .setEventListener(new TestEventListener());
        helper.init(builder.build());
        assertTrue(helper.isInitDone());

        helper.register();
        checkProviderRegistrationState(helper, provider);

        final List<ShadowApplication.Wrapper> registeredReceivers =
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
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        Configuration.Builder builder = new Configuration.Builder()
                .addProviders(
                        new MockPushProvider("providerName1", false),
                        new MockPushProvider("providerName2")
                )
                .setEventListener(new TestEventListener());
        helper.init(builder.build());

        assertTrue(helper.isInitDone());

        checkProviderRegistrationState(helper, null);

        helper.register();
        final PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);

        checkProviderRegistrationState(helper, currentProvider);
    }

    //TODO Test with PackageChangeReceiver
    @Test
    public void testRemoveHostAppPackage() throws Exception {
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        final PushProvider[] providers = {
                new MockPushProvider("provider1", "org.openpf.store1"),
                new MockPushProvider("provider2", "org.openpf.store2")
        };
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(providers)
                .setEventListener(new TestEventListener());
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

        final PushProvider oldCurrentProvider = currentProvider;
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
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        final String providerName = "providerName";

        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockPushProvider(providerName))
                .setEventListener(new TestEventListener());
        helper.init(builder.build());

        assertTrue(helper.isInitDone());

        helper.register();
        final PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);

        helper.unregister();

        assertFalse(helper.isRegistered());
        assertFalse(currentProvider.isRegistered());
        assertNull(helper.getCurrentProvider());
    }

    @Test(expected = OPFPushException.class)
    public void testRegisterTwice() throws Exception {
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockPushProvider())
                .setEventListener(new TestEventListener());
        helper.init(builder.build());
        helper.register();

        assertTrue(helper.isRegistered());
        helper.register();
    }

    @Test(expected = OPFPushException.class)
    public void testUnregisterWhileRegistrationRun() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityRegisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
        helper.unregister();
    }

    @Test
    public void testRegisterWhileRegistrationRun() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityRegisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
        helper.register();
        assertFalse(helper.isRegistered());
    }

    @Test(expected = OPFPushException.class)
    public void testRegisterWhileUnregistrationRun() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();

        assertFalse(helper.isRegistered());
        helper.register();
        assertTrue(helper.isRegistered());
        helper.unregister();
        assertTrue(helper.isRegistered());
        helper.register();
    }

    @Test
    public void testRestoreLastProvider() throws Exception {
        final MockPushProvider provider = createHelperAndRegisterWithMockProvider();

        final Settings settings = new Settings(Robolectric.application);
        final String lastProviderName = settings.getLastProviderName();
        assertNotNull(lastProviderName);
        assertEquals(provider.getName(), lastProviderName);

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(provider)
                .setEventListener(new TestEventListener());
        helper.init(builder.build());
        assertSame(provider, helper.getCurrentProvider());
        assertTrue(helper.isRegistered());
    }

    @Test
    public void testUnregisterWhileUnregistrationRun() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();

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
        final Settings settings = new Settings(Robolectric.application);
        final String lastProviderName = settings.getLastProviderName();

        assertNotNull(lastProviderName);
        assertFalse(TextUtils.isEmpty(lastProviderName));
        assertEquals(provider.getName(), lastProviderName);

        provider.setTestAvailable(false);

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(provider)
                .setEventListener(new TestEventListener());
        helper.init(builder.build());
        assertNull(helper.getCurrentProvider());
        assertFalse(helper.isRegistered());
        assertNull(settings.getLastProviderName());
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverEnable() throws Exception {
        final MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setTestAvailable(false);

        final PushProvider nextProvider = new MockPushProvider();

        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(lastProvider)
                .addProviders(nextProvider)
                .setEventListener(new TestEventListener());

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        assertTrue(helper.isRegistered());

        final PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);
        checkProviderRegistrationState(helper, currentProvider);
        assertEquals(nextProvider, currentProvider);
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverDisable() throws Exception {
        final MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setTestAvailable(false);

        restoreUnavailableProvider_RecoverDisable(lastProvider, new MockPushProvider());
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverDisable2() throws Exception {
        final MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setTestAvailable(false);

        restoreUnavailableProvider_RecoverDisable(new MockPushProvider(), lastProvider);
    }

    @Test
    public void testRestoreUnavailableProvider_RecoverEnable_AllProvidersUnavailable()
            throws Exception {

        final MockPushProvider lastProvider = createHelperAndRegisterWithMockProvider();
        lastProvider.setTestAvailable(false);

        Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockPushProvider(false))
                .addProviders(lastProvider)
                .setEventListener(new TestEventListener());

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());

        checkProviderRegistrationState(helper, null);
    }

    @Test
    public void testSendRegistrationResult_RegistrationRunning() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityRegisterProvider();
        helper.register();
        helper.getReceivedMessageHandler().onRegistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test(expected = OPFPushException.class)
    public void testSendResultWithNonExistenProvider_RegistrationRunning() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityRegisterProvider();
        helper.register();
        helper.getReceivedMessageHandler().onRegistered("123123123", "testId");
    }

    @Test(expected = IllegalStateException.class)
    public void testSendUnregistrationResult_RegistrationRunning() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityRegisterProvider();
        helper.register();
        helper.getReceivedMessageHandler().onUnregistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test(expected = IllegalStateException.class)
    public void testSendRegistrationResult_UnregistrationRunning() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();
        helper.register();
        helper.unregister();
        helper.getReceivedMessageHandler().onRegistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test
    public void testSendUnregistrationResult_UnregistrationRunning() throws Exception {
        final OPFPushHelper helper = createHelperWithInfinityUnregisterProvider();
        helper.register();
        helper.unregister();
        helper.getReceivedMessageHandler().onUnregistered(MockPushProvider.DEFAULT_NAME, "testId");
    }

    @Test(expected = OPFPushException.class)
    public void testSendMessageWithNotSendSupportProvider() throws Exception {
        createHelperAndRegisterWithMockProvider();
        final OPFPushHelper helper = OPFPushHelper.getInstance(Robolectric.application);
        assertFalse(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @Test
    public void testSendMessage() throws Exception {
        final OPFPushHelper helper = createHelperWithMockSenderPushProvider();
        helper.register();
        assertTrue(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @Test(expected = OPFPushException.class)
    public void testSendMessageWhenNotRegistered() throws Exception {
        final OPFPushHelper helper = createHelperWithMockSenderPushProvider();
        assertFalse(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @Test(expected = OPFPushException.class)
    public void testSendMessageWhenNotRegisteredWithNotSendSupportProvider() throws Exception {
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockPushProvider("providerForPref"))
                .setEventListener(new TestEventListener());

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        assertFalse(helper.canSendMessages());
        helper.sendMessage(new Message("1", new Bundle(0)));
    }

    @After
    public void tearDown() {
        new Settings(Robolectric.application).clear();
        Robolectric.packageManager.removePackage(MockPushProvider.DEFAULT_HOST_APP_PACKAGE);
    }

    private static void checkProviderRegistrationState(@NonNull OPFPushHelper helper,
                                                       @Nullable PushProvider expectedRegisteredProvider) {

        if (expectedRegisteredProvider != null) {
            assertTrue(helper.isRegistered());

            final PushProvider currentProvider = helper.getCurrentProvider();
            assertNotNull(currentProvider);
            assertSame(expectedRegisteredProvider, currentProvider);
        } else {
            assertFalse(helper.isRegistered());
            assertNull(helper.getCurrentProvider());
        }

        for (PushProvider provider : getOptions(helper).getProviders()) {
            if (provider.equals(expectedRegisteredProvider)) {
                assertTrue(provider.isRegistered());
                assertNotNull(provider.getRegistrationId());
                assertEquals(provider.getRegistrationId(), expectedRegisteredProvider.getRegistrationId());
            } else {
                assertFalse(provider.isRegistered());
                assertNull(provider.getRegistrationId());
            }
        }
    }

    private static Configuration getOptions(@NonNull OPFPushHelper helper) {
        try {
            final Field optionsField = OPFPushHelper.class.getDeclaredField("options");
            if (!optionsField.isAccessible()) {
                optionsField.setAccessible(true);
            }
            return (Configuration) optionsField.get(helper);
        } catch (Exception e) {
            throw new RuntimeException("Error get options");
        }
    }

    private static OPFPushHelper createHelperWithInfinityRegisterProvider() {
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockInfinityRegisterPushProvider())
                .setEventListener(new TestEventListener());
        helper.init(builder.build());
        return helper;
    }

    private static OPFPushHelper createHelperWithInfinityUnregisterProvider() {
        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockInfinityUnregisterPushProvider())
                .setEventListener(new TestEventListener());
        helper.init(builder.build());
        return helper;
    }

    private static MockPushProvider createHelperAndRegisterWithMockProvider() {
        final MockPushProvider provider = new MockPushProvider("providerForPref");
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(provider)
                .setEventListener(new TestEventListener());

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        helper.register();
        return provider;
    }

    private static void restoreUnavailableProvider_RecoverDisable(PushProvider... providers) {
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(providers)
                .setEventListener(new TestEventListener());

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());

        assertNull(helper.getCurrentProvider());

        checkProviderRegistrationState(helper, null);
    }

    //TODO add tests for provider callback
    private static OPFPushHelper createHelperWithMockSenderPushProvider() {
        final Configuration.Builder builder = new Configuration.Builder()
                .addProviders(new MockSenderPushProvider())
                .setEventListener(new TestEventListener());

        final OPFPushHelper helper = OPFPushHelper.newInstance(Robolectric.application);
        helper.init(builder.build());
        return helper;
    }
}
