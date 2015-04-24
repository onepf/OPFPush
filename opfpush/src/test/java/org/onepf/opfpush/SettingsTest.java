/*
 * Copyright 2012-2015 One Platform Foundation
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.model.State;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.onepf.opfpush.testutil.Util.NUM_PROVIDERS;
import static org.onepf.opfpush.testutil.Util.NUM_TESTS;
import static org.onepf.opfpush.testutil.Util.RANDOM_STRING_LENGTH;
import static org.onepf.opfpush.testutil.Util.getRandomPushProviders;
import static org.onepf.opfpush.testutil.Util.getRandomStrings;
import static org.onepf.opfpush.testutil.Util.shuffleStringArray;

/**
 * @author antonpp
 * @since 24.02.15
 */
@Config(emulateSdk = JELLY_BEAN_MR2, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
@SuppressWarnings({"checkstyle:methodname", "PMD.MethodNamingConventions", "PMD.GodClass", "PMD.TooManyMethods"})
public class SettingsTest extends Assert {

    private static final String LOG_TAG = SettingsTest.class.getSimpleName();

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_STATE = "state";
    private static final String KEY_LAST_ANDROID_ID = "android_id";
    private static final String KEY_UNREGISTERING_PROVIDER_PREFIX = "unregistering_provider_";
    private static final String KEY_REGISTERING_PROVIDER_PREFIX = "registering_provider_";
    private static final String KEY_PENDING_REGISTRATION_PROVIDER = "pending_registration_provider";
    private static final String KEY_PENDING_UNREGISTRATION_PROVIDER = "pending_unregistration_provider";

    private static final String OPF_CORE_POSTFIX = "opfpush";

    private static final Random RND = new Random();

    private PushProvider[] pushProviders;
    private SharedPreferences sharedPreferences;
    private Settings settings;

    @Before
    public void setup() {
        final Context ctx = RuntimeEnvironment.application.getApplicationContext();
        sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName() + "." + OPF_CORE_POSTFIX,
                Context.MODE_MULTI_PROCESS);
        settings = Settings.getInstance(ctx);
        pushProviders = getRandomPushProviders();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @After
    public void eraseSettingsInstance() {
        Field instanceField;
        synchronized (Settings.class) {
            try {
                instanceField = Settings.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                instanceField.set(null, null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    @Test
    public void getState_commonSituation() {
        // first is always UNREGISTERED
        assertEquals(State.UNREGISTERED, settings.getState());

        // check all states get correctly
        for (State state : State.values()) {
            sharedPreferences.edit().putInt(KEY_STATE, state.getValue()).apply();
            assertEquals(state, settings.getState());
        }
    }

    @Test
    public void getState_nullIsTreatedAsUnregistered() {
        sharedPreferences.edit().putInt(KEY_STATE, -1).apply();
        assertEquals(State.UNREGISTERED, settings.getState());
    }

    @Test
    public void saveState() {
        // check all states set correctly
        State actualState;
        State expectedState;
        final int numStates = State.values().length;
        for (int i = 0; i < NUM_TESTS; ++i) {
            expectedState = State.values()[RND.nextInt(numStates)];
            settings.saveState(expectedState);
            assertTrue(sharedPreferences.contains(KEY_STATE));
            actualState = State.fromValue(sharedPreferences.getInt(KEY_STATE, numStates));
            assertEquals(expectedState, actualState);
        }
    }

    @Test
    public void saveState_and_getState() {
        // check saveState/getState consistency
        for (State state : State.values()) {
            settings.saveState(state);
            assertEquals(settings.getState(), state);
        }
    }

    @Test
    public void saveLastProviderName() {
        PushProvider expected;
        for (int i = 0; i < NUM_TESTS; ++i) {
            expected = pushProviders[RND.nextInt(NUM_PROVIDERS)];
            settings.saveLastProvider(expected);
            assertEquals(expected.getName(), sharedPreferences.getString(KEY_LAST_PROVIDER_NAME, null));
        }
    }

    @Test
    public void saveLastProviderNameNullCase() {
        settings.saveLastProvider(pushProviders[RND.nextInt(NUM_PROVIDERS)]);
        settings.saveLastProvider(null);
        assertFalse(sharedPreferences.contains(KEY_LAST_PROVIDER_NAME));
    }

    @Test
    public void getLastProviderName() {
        String expected;
        for (int i = 0; i < NUM_TESTS; ++i) {
            expected = pushProviders[RND.nextInt(NUM_PROVIDERS)].getName();
            sharedPreferences.edit().putString(KEY_LAST_PROVIDER_NAME, expected).apply();
            assertEquals(expected, settings.getLastProviderName());
        }
    }

    @Test
    public void saveLastProviderName_and_getLastProviderName() {
        PushProvider expected;
        for (int i = 0; i < NUM_TESTS; ++i) {
            expected = pushProviders[RND.nextInt(NUM_PROVIDERS)];
            settings.saveLastProvider(expected);
            assertEquals(expected.getName(), settings.getLastProviderName());
        }
    }

    @Test
    public void saveLastAndroidId_commonSituation() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.saveLastAndroidId(randomStrings[i]);
            assertEquals(randomStrings[i], sharedPreferences.getString(KEY_LAST_ANDROID_ID, null));
        }
    }

    @Test
    public void saveLastAndroidId_passingNullClearsPreferences() {
        // test null case
        sharedPreferences.edit().putString(KEY_LAST_ANDROID_ID, "notNull").apply();
        settings.saveLastAndroidId(null);
        assertFalse(sharedPreferences.contains(KEY_LAST_ANDROID_ID));
    }

    @Test
    public void getLastAndroidId() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            sharedPreferences.edit().putString(KEY_LAST_ANDROID_ID, randomStrings[i]).apply();
            assertEquals(randomStrings[i], settings.getLastAndroidId());
        }
    }

    @Test
    public void getLastAndroidId_and_saveLastAndroidId() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.saveLastAndroidId(randomStrings[i]);
            assertEquals(randomStrings[i], settings.getLastAndroidId());
        }
    }

    @Test
    public void isProviderUnregistrationPerforming_false() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            assertFalse(settings.isProviderUnregistrationPerforming(randomStrings[i]));
        }
    }

    @Test
    public void isProviderUnregistrationPerforming_true() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            sharedPreferences.edit().putBoolean(
                    KEY_UNREGISTERING_PROVIDER_PREFIX + randomStrings[i].toLowerCase(Locale.US),
                    true
            ).apply();
        }

        final List<String> mixedRandomStrings = shuffleStringArray(randomStrings);
        for (int i = 0; i < NUM_TESTS; ++i) {
            assertTrue(settings.isProviderUnregistrationPerforming(mixedRandomStrings.get(i)));
        }
    }

    @Test
    public void saveUnregisteringProvider_commonCase() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.saveUnregisteringProvider(randomStrings[i]);
        }

        final List<String> mixedRandomStrings = shuffleStringArray(randomStrings);
        for (int i = 0; i < NUM_TESTS; ++i) {
            assertTrue(sharedPreferences.getBoolean(
                    KEY_UNREGISTERING_PROVIDER_PREFIX + mixedRandomStrings.get(i).toLowerCase(Locale.US),
                    false
            ));
        }
    }

    @Test
    public void removeUnregisteringProvider_commonCase() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            sharedPreferences.edit().putBoolean(
                    KEY_UNREGISTERING_PROVIDER_PREFIX + randomStrings[i].toLowerCase(Locale.US),
                    true
            ).apply();
        }

        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.removeUnregisteringProvider(randomStrings[i]);
        }

        for (int i = 0; i < NUM_TESTS; ++i) {
            assertFalse(sharedPreferences.contains(
                    KEY_UNREGISTERING_PROVIDER_PREFIX + randomStrings[i].toLowerCase(Locale.US)
            ));
        }
    }

    @Test
    public void isProviderRegistrationPerforming_false() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            assertFalse(settings.isProviderRegistrationPerforming(randomStrings[i]));
        }
    }

    @Test
    public void isProviderRegistrationPerforming_true() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            sharedPreferences.edit().putBoolean(
                    KEY_REGISTERING_PROVIDER_PREFIX + randomStrings[i].toLowerCase(Locale.US),
                    true
            ).apply();
        }

        final List<String> mixedRandomStrings = shuffleStringArray(randomStrings);
        for (int i = 0; i < NUM_TESTS; ++i) {
            assertTrue(settings.isProviderRegistrationPerforming(mixedRandomStrings.get(i)));
        }
    }

    @Test
    public void saveRegisteringProvider_commonCase() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.saveRegisteringProvider(randomStrings[i]);
        }

        final List<String> mixedRandomStrings = shuffleStringArray(randomStrings);
        for (int i = 0; i < NUM_TESTS; ++i) {
            assertTrue(sharedPreferences.getBoolean(
                    KEY_REGISTERING_PROVIDER_PREFIX + mixedRandomStrings.get(i).toLowerCase(Locale.US),
                    false
            ));
        }
    }

    @Test
    public void removeRegisteringProvider_commonCase() {
        final String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            sharedPreferences.edit().putBoolean(
                    KEY_REGISTERING_PROVIDER_PREFIX + randomStrings[i].toLowerCase(Locale.US),
                    true
            ).apply();
        }

        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.removeRegisteringProvider(randomStrings[i]);
        }

        for (int i = 0; i < NUM_TESTS; ++i) {
            assertFalse(sharedPreferences.contains(
                    KEY_REGISTERING_PROVIDER_PREFIX + randomStrings[i].toLowerCase(Locale.US)
            ));
        }
    }

    @Test
    public void testClear() {
        sharedPreferences.edit()
                .putString(KEY_LAST_PROVIDER_NAME, "not empty")
                .putInt(KEY_STATE, RND.nextInt())
                .putString(KEY_LAST_ANDROID_ID, "string")
                .apply();
        settings.clear();
        assertFalse(sharedPreferences.contains(KEY_LAST_PROVIDER_NAME));
        assertFalse(sharedPreferences.contains(KEY_STATE));
        assertFalse(sharedPreferences.contains(KEY_LAST_ANDROID_ID));
    }

    @Test
    public void testSavePendingRegistrationProvider() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.savePendingRegistrationProvider(randomStrings[i]);
            assertEquals(randomStrings[i], sharedPreferences.getString(KEY_PENDING_REGISTRATION_PROVIDER, ""));
        }
    }

    @Test
    public void testRemovePendingRegistrationProvider() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.savePendingRegistrationProvider(randomStrings[i]);
            assertTrue(sharedPreferences.contains(KEY_PENDING_REGISTRATION_PROVIDER));
            settings.removePendingRegistrationProvider();
            assertFalse(sharedPreferences.contains(KEY_PENDING_REGISTRATION_PROVIDER));
        }
    }

    @Test
    public void testGetPendingRegistrationProvider() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.savePendingRegistrationProvider(randomStrings[i]);
            assertTrue(sharedPreferences.contains(KEY_PENDING_REGISTRATION_PROVIDER));
            assertEquals(randomStrings[i], settings.getPendingRegistrationProvider());

        }
    }

    @Test
    public void testSavePendingUnregistrationProvider() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.savePendingUnregistrationProvider(randomStrings[i]);
            assertEquals(randomStrings[i], sharedPreferences.getString(KEY_PENDING_UNREGISTRATION_PROVIDER, ""));
        }
    }

    @Test
    public void testRemovePendingUnregistrationProvider() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.savePendingUnregistrationProvider(randomStrings[i]);
            assertTrue(sharedPreferences.contains(KEY_PENDING_UNREGISTRATION_PROVIDER));
            settings.removePendingUnregistrationProvider();
            assertFalse(sharedPreferences.contains(KEY_PENDING_UNREGISTRATION_PROVIDER));
        }
    }

    @Test
    public void testGetPendingUnregistrationProvider() {
        String[] randomStrings = getRandomStrings(NUM_TESTS, RANDOM_STRING_LENGTH);
        for (int i = 0; i < NUM_TESTS; ++i) {
            settings.savePendingUnregistrationProvider(randomStrings[i]);
            assertTrue(sharedPreferences.contains(KEY_PENDING_UNREGISTRATION_PROVIDER));
            assertEquals(randomStrings[i], settings.getPendingUnregistrationProvider());

        }
    }

    @Test
    public void testGetInstance() {
        eraseSettingsInstance();
        settings = Settings.getInstance(RuntimeEnvironment.application.getApplicationContext());
        assertEquals(settings, Settings.getInstance(RuntimeEnvironment.application.getApplicationContext()));
    }


}
