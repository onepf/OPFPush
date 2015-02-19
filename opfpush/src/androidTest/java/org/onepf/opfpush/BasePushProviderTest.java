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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.mock.MockPushProvider;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 10/9/14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class BasePushProviderTest {

    @Test
    public void testIsAvailable_HostAppInstalled() {
        final PushProvider mockPushProvider = new MockPushProvider();
        Robolectric.packageManager.addPackage(mockPushProvider.getHostAppPackage());
        assertTrue(mockPushProvider.isAvailable());
    }

    @Test
    public void testIsAvailable_NoHostApp() {
        assertFalse(new MockPushProvider().isAvailable());
    }

    @Test
    public void testIsAvailable() {
        final PushProvider mockPushProvider = new MockPushProvider();
        assertFalse(mockPushProvider.isAvailable());

        final String hostAppPackage = mockPushProvider.getHostAppPackage();

        Robolectric.packageManager.addPackage(hostAppPackage);
        assertTrue(mockPushProvider.isAvailable());

        Robolectric.packageManager.removePackage(hostAppPackage);
        assertFalse(mockPushProvider.isAvailable());
    }

    @Test
    public void testCreate() {
        final PushProvider provider = new MockPushProvider();
        assertFalse(provider.isAvailable());
        assertEquals(MockPushProvider.DEFAULT_NAME, provider.getName());
        assertEquals(MockPushProvider.DEFAULT_HOST_APP_PACKAGE, provider.getHostAppPackage());
    }

    @Test
    public void testCheckManifest() {
        final PushProvider provider = new MockPushProvider();
        assertTrue(provider.checkManifest());
    }
}
