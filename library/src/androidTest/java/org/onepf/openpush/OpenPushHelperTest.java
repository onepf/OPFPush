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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.openpush.shadow.ShadowPackageManager;
import org.onepf.openpush.util.OpenPushHelperKeeper;
import org.onepf.openpush.util.PackageInfoKeeper;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OpenPushHelperTest {

    @Before
    public void setup() {
        String hostAppPackage = new MockPushProvider(Robolectric.application).getHostAppPackage();
        PackageInfoKeeper.installPackage(hostAppPackage);
    }

    @Test
    public void testInit() {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);
        assertFalse(openPushHelper.isInitDone());
        assertEquals(OpenPushHelper.State.NONE, openPushHelper.getState());
        openPushHelper.init(builder.build());
        assertTrue(openPushHelper.isInitDone());
        assertEquals(OpenPushHelper.State.NONE, openPushHelper.getState());
    }

    @Test(expected = OpenPushException.class)
    public void testInitTwice() {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);
        assertFalse(openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
        assertTrue(openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
    }

    @Test(expected = OpenPushException.class)
    public void testUnregisterWithoutRegister() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        Options options = builder.build();
        helper.init(options);

        helper.unregister();
    }

    @Test
    public void testRegister() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        final String providerName = "providerName";
        builder.addProviders(new MockPushProvider(Robolectric.application, providerName));
        Options options = builder.build();
        helper.init(options);
        assertTrue(helper.isInitDone());
        assertEquals(OpenPushHelper.State.NONE, helper.getState());

        helper.register();
        assertEquals(OpenPushHelper.State.RUNNING, helper.getState());
        assertEquals(providerName, helper.getCurrentProviderName());
        assertNotNull(helper.getCurrentProviderRegistrationId());
    }

    @Test(expected = OpenPushException.class)
    public void testUnregisterBeforeInit() {
        OpenPushHelperKeeper.getNewInstance(Robolectric.application).unregister();
    }

    @Test(expected = OpenPushException.class)
    public void testRegisterBeforeInit() {
        OpenPushHelperKeeper.getNewInstance(Robolectric.application).register();
    }

    @Test
    public void testRegisterSecondProvider() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);
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
        assertEquals(OpenPushHelper.State.NONE, helper.getState());

        assertFalse(provider1.isRegistered());
        assertFalse(provider2.isRegistered());

        helper.register();
        assertEquals(OpenPushHelper.State.RUNNING, helper.getState());
        assertEquals(provider2.getName(), helper.getCurrentProviderName());
        assertEquals(provider2.getRegistrationId(),
                helper.getCurrentProviderRegistrationId());

        assertFalse(provider1.isRegistered());
        assertTrue(provider2.isRegistered());
    }

    @Config(shadows = {ShadowPackageManager.class})
    @Test
    public void testRemoveHostAppPackage() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);
        assertFalse(helper.isInitDone());

        Options.Builder builder = new Options.Builder();
        MockPushProvider provider1
                = new MockPushProvider(Robolectric.application, "provider1", "org.openpf.store1");
        PackageInfoKeeper.installPackage(provider1.getHostAppPackage());
        builder.addProviders(provider1);
        MockPushProvider provider2
                = new MockPushProvider(Robolectric.application, "provider2", "org.openpf.store2");
        PackageInfoKeeper.installPackage(provider2.getHostAppPackage());
        builder.addProviders(provider2);
        helper.init(builder.build());

        assertFalse(provider1.isRegistered());
        assertFalse(provider2.isRegistered());

        helper.register();
        assertEquals(OpenPushHelper.State.RUNNING, helper.getState());
        assertEquals(provider1.getName(), helper.getCurrentProviderName());
        assertEquals(provider1.getRegistrationId(),
                helper.getCurrentProviderRegistrationId());

        assertTrue(provider1.isRegistered());
        assertFalse(provider2.isRegistered());

        provider1.setHostAppEnable(false);
        helper.onBecameUnavailable(provider1);
        assertEquals(OpenPushHelper.State.RUNNING, helper.getState());
        assertEquals(provider2.getName(), helper.getCurrentProviderName());
        assertEquals(provider2.getRegistrationId(),
                helper.getCurrentProviderRegistrationId());

        assertFalse(provider1.isRegistered());
        assertTrue(provider2.isRegistered());

        PackageInfoKeeper.uninstallPackage(provider1.getHostAppPackage());
        PackageInfoKeeper.uninstallPackage(provider2.getHostAppPackage());

        assertFalse(provider1.isRegistered());
        assertTrue(provider2.isRegistered());
    }

    @Test
    public void testUnregister() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        final String providerName = "providerName";
        builder.addProviders(new MockPushProvider(Robolectric.application, providerName));
        Options options = builder.build();
        helper.init(options);
        assertTrue(helper.isInitDone());
        assertEquals(OpenPushHelper.State.NONE, helper.getState());

        helper.register();
        assertEquals(OpenPushHelper.State.RUNNING, helper.getState());
        assertEquals(providerName, helper.getCurrentProviderName());
        assertNotNull(helper.getCurrentProviderRegistrationId());

        helper.unregister();
        assertEquals(OpenPushHelper.State.NONE, helper.getState());
        assertNull(helper.getCurrentProviderName());
        assertNull(helper.getCurrentProviderRegistrationId());
    }

    @Test(expected = OpenPushException.class)
    public void testRegisterTwice() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        Options options = builder.build();
        helper.init(options);
        helper.register();
        assertEquals(OpenPushHelper.State.RUNNING, helper.getState());
        helper.register();
    }
}