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

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OpenPushHelperTest {

    @Test
    public void testInit() {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);
        assertEquals(false, openPushHelper.isInitDone());
        assertEquals(OpenPushHelper.State.STATE_NONE, openPushHelper.getState());
        openPushHelper.init(builder.build());
        assertEquals(true, openPushHelper.isInitDone());
        assertEquals(OpenPushHelper.State.STATE_NONE, openPushHelper.getState());
    }

    @Test(expected = OpenPushException.class)
    public void testInitTwice() {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);
        assertEquals(false, openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
        assertEquals(true, openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
    }

    @Test
    public void testRegister() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        Options options = builder.build();
        helper.init(options);
        assertEquals(true, helper.isInitDone());
        assertEquals(OpenPushHelper.State.STATE_NONE, helper.getState());

        helper.register();
        assertEquals(OpenPushHelper.State.STATE_RUNNING, helper.getState());
        assertEquals(MockPushProvider.NAME, helper.getCurrentProviderName());
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
    public void testUnregister() {
        OpenPushHelper helper = OpenPushHelperKeeper.getNewInstance(Robolectric.application);

        Options.Builder builder = new Options.Builder();
        builder.addProviders(new MockPushProvider(Robolectric.application));
        Options options = builder.build();
        helper.init(options);
        assertEquals(true, helper.isInitDone());
        assertEquals(OpenPushHelper.State.STATE_NONE, helper.getState());

        helper.register();
        assertEquals(OpenPushHelper.State.STATE_RUNNING, helper.getState());
        assertEquals(MockPushProvider.NAME, helper.getCurrentProviderName());
        assertNotNull(helper.getCurrentProviderRegistrationId());

        helper.unregister();
        assertEquals(OpenPushHelper.State.STATE_NONE, helper.getState());
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
        assertEquals(OpenPushHelper.State.STATE_RUNNING, helper.getState());
        helper.register();
    }
}
