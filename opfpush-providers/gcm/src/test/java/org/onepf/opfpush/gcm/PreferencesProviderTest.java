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
import org.onepf.opfutils.OPFUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by antonpp on 02.03.15.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class PreferencesProviderTest extends Assert {

    private static final String GCM_POSTFIX = "gcm";
    private static final String KEY_APP_VERSION = "app_version";
    private static final String KEY_REGISTRATION_ID = "registration_id";

    private static final Random RND = new Random();

    private static final int NUM_TESTS = 100;

    private Context ctx;
    private PreferencesProvider preferencesProvider;
    private OPFPreferences preferences;

    @Before
    public void setup() {
        ctx = Robolectric.application.getApplicationContext();
        preferencesProvider = PreferencesProvider.getInstance(ctx);
        preferences = new OPFPreferences(ctx, GCM_POSTFIX);
    }

    private void postSetupUpdateAppVersion() {
        preferences.put(KEY_APP_VERSION, OPFUtils.getAppVersion(ctx));
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

    @Test
    public void testGetRegistrationId() {
        postSetupUpdateAppVersion();

        String expected;
        for (int i = 0; i < NUM_TESTS; ++i) {
            expected = String.format("id%d", i);
            preferences.put(KEY_REGISTRATION_ID, expected);
            assertEquals(expected, preferencesProvider.getRegistrationId());
        }
    }

    @Test
    public void testGetRegistrationIdNullCase() {
        postSetupUpdateAppVersion();

        // registration id must be null if it is not set
        assertNull(preferencesProvider.getRegistrationId());

        // registration Id after reset must be null
        preferencesProvider.saveRegistrationId("notNullRegistrationId");
        preferencesProvider.reset();
        assertNull(preferencesProvider.getRegistrationId());
    }

    @Test
    public void testSaveRegistrationId() {

        // not null registration id should be saved
        String expected;
        for (int i = 0; i < NUM_TESTS; ++i) {
            expected = String.format("id%d", i);
            preferencesProvider.saveRegistrationId(expected);
            assertEquals(expected, preferences.getString(KEY_REGISTRATION_ID));
        }

        // null registration id must erase previous value
        preferencesProvider.saveRegistrationId(null);
        assertFalse(preferences.contains(KEY_REGISTRATION_ID));
    }

    @Test
    public void testReset() {
        postSetupUpdateAppVersion();

        for (int i = 0; i < NUM_TESTS; ++i) {
            preferences.put(KEY_APP_VERSION, RND.nextInt());
            preferences.put(KEY_REGISTRATION_ID, String.format("regId%d", RND.nextInt()));
            preferencesProvider.reset();
            assertNull(preferences.getString(KEY_REGISTRATION_ID));
            assertNull(preferences.getInt(KEY_APP_VERSION));
        }
    }
}
