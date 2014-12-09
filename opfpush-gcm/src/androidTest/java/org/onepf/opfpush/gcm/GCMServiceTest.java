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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.*;
import org.onepf.opfpush.gcm.mock.GCMServiceMock;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.listener.SimpleEventListener;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.model.State;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.onepf.opfpush.model.OPFError.AUTHENTICATION_FAILED;
import static org.onepf.opfpush.model.OPFError.SERVICE_NOT_AVAILABLE;
import static org.onepf.opfpush.OPFPushLog.LOGD;
import static org.onepf.opfpush.gcm.GCMConstants.ACTION_REGISTRATION_CALLBACK;
import static org.onepf.opfpush.gcm.GCMConstants.ACTION_UNREGISTRATION_CALLBACK;
import static org.onepf.opfpush.gcm.GCMConstants.ERROR_AUTHENTICATION_FAILED;
import static org.onepf.opfpush.gcm.GCMConstants.ERROR_SERVICE_NOT_AVAILABLE;
import static org.onepf.opfpush.gcm.GCMConstants.EXTRA_ERROR_ID;
import static org.onepf.opfpush.gcm.GCMConstants.EXTRA_REGISTRATION_ID;
import static org.onepf.opfpush.gcm.util.TestConstants.REGISTRATION_ID;
import static org.onepf.opfpush.gcm.util.TestConstants.SENDER_ID;
import static org.onepf.opfpush.model.State.REGISTERING;
import static org.onepf.opfpush.model.State.UNREGISTERING;

/**
 * @author Roman Savin
 * @since 03.12.14
 */
@Config(
        emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2
)
@RunWith(RobolectricTestRunner.class)
//TODO look how messageType is stored in intent and implement tests for onMessage and onDeletedMessage functionality
public class GCMServiceTest {

    private static final String PREF_NAME = "org.onepf.openpush";
    private static final String KEY_STATE = "state";

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testRegisteredIntent() {
        final OPFPushHelper helper = initOPFPushHelper(
                new SimpleEventListener() {
                    @Override
                    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
                        LOGD("onRegistered(%1$s, %2$s)", providerName, registrationId);
                        assertEquals(GCMProvider.NAME, providerName);
                        assertEquals(REGISTRATION_ID, registrationId);
                    }

                    @Override
                    public void onRegistrationError(@NonNull String providerName, @NonNull OPFError error) {
                        LOGD("onRegistrationError(%1$s, %2$s)", providerName, error);
                        assertFalse(true); //Test failed because we get registration error.
                    }
                }
        );

        applyStateRegistering();

        final Intent serviceIntent = new Intent(ACTION_REGISTRATION_CALLBACK);
        serviceIntent.setComponent(new ComponentName(Robolectric.application, GCMServiceMock.class));
        serviceIntent.putExtra(EXTRA_REGISTRATION_ID, REGISTRATION_ID);

        final GCMServiceMock service = new GCMServiceMock();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
    }

    @Test
    public void testUnregisteredIntent() {
        final OPFPushHelper helper = initOPFPushHelper(
                new SimpleEventListener() {
                    @Override
                    public void onUnregistered(@NonNull String providerName, @NonNull String registrationId) {
                        LOGD("onUnregistered(%1$s, %2$s)", providerName, registrationId);
                        assertEquals(GCMProvider.NAME, providerName);
                        assertEquals(REGISTRATION_ID, registrationId);
                    }

                    @Override
                    public void onUnregistrationError(@NonNull String providerName, @NonNull OPFError error) {
                        LOGD("onUnregistrationError(%1$s, %2$s)", providerName, error);
                        assertFalse(true); //Test failed because we get registration error.
                    }
                }
        );

        applyStateUnregistering();

        final Intent serviceIntent = new Intent(ACTION_UNREGISTRATION_CALLBACK);
        serviceIntent.setComponent(new ComponentName(Robolectric.application, GCMServiceMock.class));
        serviceIntent.putExtra(EXTRA_REGISTRATION_ID, REGISTRATION_ID);

        final GCMServiceMock service = new GCMServiceMock();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
    }

    @Test
    public void testRegisteredErrorIntent_ServiceNotAvailable() {
        final OPFPushHelper helper = initOPFPushHelper(
                new SimpleEventListener() {
                    @Override
                    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
                        LOGD("onRegistered(%1$s, %2$s)", providerName, registrationId);
                        assertFalse(true); //Test failed because we get registration error.
                    }

                    @Override
                    public void onRegistrationError(@NonNull String providerName, @NonNull OPFError error) {
                        LOGD("onRegistrationError(%1$s, %2$s)", providerName, error);
                        assertEquals(SERVICE_NOT_AVAILABLE, error);
                    }
                }
        );

        applyStateRegistering();

        final Intent serviceIntent = new Intent(ACTION_REGISTRATION_CALLBACK);
        serviceIntent.setComponent(new ComponentName(Robolectric.application, GCMServiceMock.class));
        serviceIntent.putExtra(EXTRA_ERROR_ID, ERROR_SERVICE_NOT_AVAILABLE);

        final GCMServiceMock service = new GCMServiceMock();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
    }

    @Test
    public void testRegisteredErrorIntent_AuthenticationFailed() {
        final OPFPushHelper helper = initOPFPushHelper(
                new SimpleEventListener() {
                    @Override
                    public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
                        LOGD("onRegistered(%1$s, %2$s)", providerName, registrationId);
                        assertFalse(true); //Test failed because we get registration error.
                    }

                    @Override
                    public void onRegistrationError(@NonNull String providerName, @NonNull OPFError error) {
                        LOGD("onRegistrationError(%1$s, %2$s)", providerName, error);
                        assertEquals(AUTHENTICATION_FAILED, error);
                    }
                }
        );

        applyStateRegistering();

        final Intent serviceIntent = new Intent(ACTION_REGISTRATION_CALLBACK);
        serviceIntent.setComponent(new ComponentName(Robolectric.application, GCMServiceMock.class));
        serviceIntent.putExtra(EXTRA_ERROR_ID, ERROR_AUTHENTICATION_FAILED);

        final GCMServiceMock service = new GCMServiceMock();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
    }

    @Test
    public void testUnregisteredIntent_ServiceNotAvailable() {
        final OPFPushHelper helper = initOPFPushHelper(
                new SimpleEventListener() {
                    @Override
                    public void onUnregistered(@NonNull String providerName, @NonNull String registrationId) {
                        LOGD("onUnregistered(%1$s, %2$s)", providerName, registrationId);
                        assertFalse(true); //Test failed because we get registration error.
                    }

                    @Override
                    public void onUnregistrationError(@NonNull String providerName, @NonNull OPFError error) {
                        LOGD("onUnregistrationError(%1$s, %2$s)", providerName, error);
                        assertEquals(SERVICE_NOT_AVAILABLE, error);
                    }
                }
        );

        applyStateUnregistering();

        final Intent serviceIntent = new Intent(ACTION_UNREGISTRATION_CALLBACK);
        serviceIntent.setComponent(new ComponentName(Robolectric.application, GCMServiceMock.class));
        serviceIntent.putExtra(EXTRA_ERROR_ID, ERROR_SERVICE_NOT_AVAILABLE);

        final GCMServiceMock service = new GCMServiceMock();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
    }

    @Test
    public void testUnregisteredIntent_AuthenticationFailed() {
        final OPFPushHelper helper = initOPFPushHelper(
                new SimpleEventListener() {
                    @Override
                    public void onUnregistered(@NonNull String providerName, @NonNull String registrationId) {
                        LOGD("onUnregistered(%1$s, %2$s)", providerName, registrationId);
                        assertFalse(true); //Test failed because we get registration error.
                    }

                    @Override
                    public void onUnregistrationError(@NonNull String providerName, @NonNull OPFError error) {
                        LOGD("onUnregistrationError(%1$s, %2$s)", providerName, error);
                        assertEquals(AUTHENTICATION_FAILED, error);
                    }
                }
        );

        applyStateUnregistering();

        final Intent serviceIntent = new Intent(ACTION_UNREGISTRATION_CALLBACK);
        serviceIntent.setComponent(new ComponentName(Robolectric.application, GCMServiceMock.class));
        serviceIntent.putExtra(EXTRA_ERROR_ID, ERROR_AUTHENTICATION_FAILED);

        final GCMServiceMock service = new GCMServiceMock();
        service.onCreate();
        service.onHandleIntent(serviceIntent);
    }

    private OPFPushHelper initOPFPushHelper(@NonNull final EventListener eventListener) {
        final Options.Builder builder = new Options.Builder()
                .addProviders(new GCMProvider(Robolectric.application, SENDER_ID))
                .setEventListener(eventListener);
        final OPFPushHelper helper = newOPFPushHelperInstance();
        helper.init(builder.build());
        assertTrue(helper.isInitDone());
        return helper;
    }

    private OPFPushHelper newOPFPushHelperInstance() {
        try {
            final Method newInstanceMethod =
                    OPFPushHelper.class.getDeclaredMethod("newInstance", Context.class);

            if (!newInstanceMethod.isAccessible()) {
                newInstanceMethod.setAccessible(true);
            }
            return (OPFPushHelper) newInstanceMethod.invoke(null, Robolectric.application);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error create new instance of OPFPushHelper");
        }
    }

    @After
    public void tearDown() {
        final Context appContext = Robolectric.getShadowApplication().getApplicationContext();
        final SharedPreferences preferences = appContext
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    private void applyStateRegistering() {
        applyState(REGISTERING);
    }

    private void applyStateUnregistering() {
        applyState(UNREGISTERING);
    }

    private void applyState(@NonNull final State state) {
        final Context appContext = Robolectric.getShadowApplication().getApplicationContext();
        final SharedPreferences preferences = appContext
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_STATE, state.getValue()).apply();
    }
}
