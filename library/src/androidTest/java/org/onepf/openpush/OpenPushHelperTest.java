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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        MockPushProvider provider1
                = new MockPushProvider(Robolectric.application, "providerName1", false);
        builder.addProviders(provider1);
        MockPushProvider provider2
                = new MockPushProvider(Robolectric.application, "providerName2");
        builder.addProviders(provider2);
        helper.init(builder.build());

        assertTrue(helper.isInitDone());

        assertFalse(provider1.isRegistered());
        assertFalse(provider2.isRegistered());

        helper.register();
        PushProvider provider = helper.getCurrentProvider();
        assertNotNull(provider);
        assertTrue(provider.isRegistered());
        assertEquals(provider2.getName(), provider.getName());
        assertEquals(provider2.getRegistrationId(), provider.getRegistrationId());

        assertFalse(provider1.isRegistered());
        assertTrue(provider2.isRegistered());
    }

    @Test
    public void testRemoveHostAppPackage() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        Options.Builder builder = new Options.Builder();
        MockPushProvider provider1
                = new MockPushProvider(Robolectric.application, "provider1", "org.openpf.store1");
        Robolectric.packageManager.addPackage(provider1.getHostAppPackage());
        builder.addProviders(provider1);
        MockPushProvider provider2
                = new MockPushProvider(Robolectric.application, "provider2", "org.openpf.store2");
        Robolectric.packageManager.addPackage(provider2.getHostAppPackage());
        builder.addProviders(provider2);
        helper.init(builder.build());

        assertFalse(provider1.isRegistered());
        assertFalse(provider2.isRegistered());

        helper.register();
        assertNotNull(helper.getCurrentProvider());
        assertEquals(provider1.getName(), helper.getCurrentProvider().getName());
        assertEquals(provider1.getRegistrationId(), helper.getCurrentProvider().getRegistrationId());

        assertTrue(provider1.isRegistered());
        assertFalse(provider2.isRegistered());

        Robolectric.packageManager.removePackage(provider1.getHostAppPackage());
        helper.onUnavailable(provider1);
        assertNotNull(helper.getCurrentProvider());
        assertEquals(provider2.getName(), helper.getCurrentProvider().getName());
        assertEquals(provider2.getRegistrationId(), helper.getCurrentProvider().getRegistrationId());

        assertFalse(provider1.isRegistered());
        assertTrue(provider2.isRegistered());

        Robolectric.packageManager.removePackage(provider1.getHostAppPackage());
        Robolectric.packageManager.removePackage(provider2.getHostAppPackage());

        assertFalse(provider1.isRegistered());
        assertTrue(provider2.isRegistered());
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
        PushProvider currentProvider = helper.getCurrentProvider();
        assertNotNull(currentProvider);
        assertTrue(helper.isRegistered());
        assertEquals(providerName, helper.getCurrentProvider().getName());
        assertNotNull(helper.getCurrentProvider().getRegistrationId());

        helper.unregister();
        assertFalse(currentProvider.isRegistered());
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

    private static OpenPushHelper createHelperWithInfinityRegisterProvider() {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockInfinityRegisterPushProvider(Robolectric.application));
        helper.init(builder.build());
        return helper;
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

    private static OpenPushHelper createHelperWithInfinityUnregisterProvider() {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockInfinityUnregisterPushProvider(Robolectric.application));
        helper.init(builder.build());
        return helper;
    }

    @Test
    public void testRestoreLastProvider() throws Exception {
        OpenPushHelper helper = OpenPushHelper.getNewInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        MockPushProvider provider
                = new MockPushProvider(Robolectric.application, "providerForPref");
        builder.addProviders(provider);
        Options options = builder.build();
        helper.init(options);

        assertFalse(helper.isRegistered());
        helper.register();
        assertTrue(helper.isRegistered());
        assertSame(provider, helper.getCurrentProvider());

        SharedPreferences prefs =
                Robolectric.application.getSharedPreferences(OpenPushHelper.PREF_NAME, Context.MODE_PRIVATE);
        String lastProviderName = prefs.getString(OpenPushHelper.KEY_LAST_PROVIDER_NAME, null);
        assertNotNull(lastProviderName);
        assertEquals(provider.getName(), lastProviderName);

        helper = OpenPushHelper.getNewInstance(Robolectric.application);
        helper.init(options);
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

    @After
    public void tearDown() {
        Robolectric.packageManager.removePackage(MockPushProvider.DEFAULT_HOST_APP_PACKAGE);
    }
}
