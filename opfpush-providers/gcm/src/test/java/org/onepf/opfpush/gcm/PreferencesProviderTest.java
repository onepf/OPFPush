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

import android.content.Context;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfutils.OPFPreferences;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

/**
 * Created by antonpp on 02.03.15.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class PreferencesProviderTest {

    private static final String GCM_POSTFIX = "gcm";
    private static final String KEY_APP_VERSION = "app_version";

    private Context ctx;
    private PreferencesProvider preferencesProvider;
    private OPFPreferences preferences;

    @Before
    public void setup() {
        ctx = Robolectric.application.getApplicationContext();
        preferencesProvider = PreferencesProvider.getInstance(ctx);
        preferences = new OPFPreferences(ctx, GCM_POSTFIX);
    }

    @After
    public void erasePreferencesProviderInstance() {
        Field instanceField;
        synchronized (PreferencesProvider.class) {
            try {
                instanceField = PreferencesProvider.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                instanceField.set(null, null);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testChangeApplicationVersion() {
        final int appVersion1 = 1;
        final int appVersion2 = 2;
        final String registrationIdBefore = "id1";

        // save registration Id with one appVersion
        preferences.put(KEY_APP_VERSION, appVersion1);
        preferencesProvider.saveRegistrationId(registrationIdBefore);

        // get registration Id with another appVersion
        preferences.put(KEY_APP_VERSION, appVersion2);
        final String registrarionIdAfter = preferencesProvider.getRegistrationId();

        Assert.assertNotSame(registrationIdBefore, registrarionIdAfter);
    }

}
