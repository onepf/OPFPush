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

package org.onepf.opfpush.gcm;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.gcm.shadow.ShadowGooglePlayServiceUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.onepf.opfpush.gcm.util.TestConstants.SENDER_ID;

/**
 * @author Kirill Rozov
 * @since 10/8/14.
 */
@Config(
        emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2,
        shadows = {ShadowGooglePlayServiceUtil.class},
        reportSdk = Build.VERSION_CODES.GINGERBREAD_MR1
)
@RunWith(RobolectricTestRunner.class)
@SuppressWarnings({"checkstyle:methodname", "PMD.MethodNamingConventions"})
public class GCMProviderGingerbreadTest extends GCMProviderTestBase {

    @Test
    public void testCheckAvailable_LastGMSInstalled_NoGoogleAccount() throws Exception {
        addLatestGMSServiceApp();
        final GCMProvider provider = new GCMProvider(Robolectric.application, SENDER_ID);
        assertFalse(provider.isAvailable());
        removeGMCServiceApp();
    }

    @Test
    public void testCheckAvailable_LastGMSInstalled_WithGoogleAccount() throws Exception {
        addLatestGMSServiceApp();
        addGoogleAccount();
        final GCMProvider provider = new GCMProvider(Robolectric.application, SENDER_ID);
        assertTrue(provider.isAvailable());
        removeGoogleAccount();
        removeGMCServiceApp();
    }

    @Test
    public void testCheckAvailable_NoGMSInstalled_NoGoogleAccount() throws Exception {
        final GCMProvider provider = new GCMProvider(Robolectric.application, SENDER_ID);
        assertFalse(provider.isAvailable());
    }

    @Test
    public void testCheckAvailable_NoGMSInstalled_WithGoogleAccount() throws Exception {
        addGoogleAccount();
        final GCMProvider provider = new GCMProvider(Robolectric.application, SENDER_ID);
        assertFalse(provider.isAvailable());
        removeGoogleAccount();
    }
}
