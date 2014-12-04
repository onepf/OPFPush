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

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onepf.opfpush.gcm.GCMConstants.ACTION_REGISTRATION;
import static org.onepf.opfpush.gcm.GCMConstants.ACTION_REGISTRATION_CALLBACK;
import static org.onepf.opfpush.gcm.GCMConstants.ACTION_UNREGISTRATION_CALLBACK;
import static org.onepf.opfpush.gcm.GCMConstants.EXTRA_REGISTRATION_ID;
import static org.onepf.opfpush.gcm.util.TestConstants.MESSAGE_EXTRA_KEY;
import static org.onepf.opfpush.gcm.util.TestConstants.REGISTRATION_ID;

/**
 * @author Roman Savin
 * @since 03.12.14
 */
@Config(
        emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2
)
@RunWith(RobolectricTestRunner.class)
public class GCMReceiverTest {

    private static final String ACTION_RECEIVE = "com.google.android.c2dm.intent.RECEIVE";

    private GCMReceiver gcmReceiver = new GCMReceiver();

    @Before
    public void setup() {
        ShadowLog.stream = System.out;

        final IntentFilter receiveFilter = new IntentFilter(ACTION_RECEIVE);
        receiveFilter.addCategory(BuildConfig.APPLICATION_ID);

        final IntentFilter actionRegistrationFilter = new IntentFilter(ACTION_REGISTRATION);
        actionRegistrationFilter.addCategory(BuildConfig.APPLICATION_ID);

        final IntentFilter registrationUnregistrationFilter = new IntentFilter();
        registrationUnregistrationFilter.addAction(ACTION_REGISTRATION_CALLBACK);
        registrationUnregistrationFilter.addAction(ACTION_UNREGISTRATION_CALLBACK);

        final String permissionSend = "com.google.android.c2dm.permission.SEND";
        Robolectric.application.registerReceiver(gcmReceiver, receiveFilter, permissionSend, null);
        Robolectric.application.registerReceiver(gcmReceiver, actionRegistrationFilter, permissionSend, null);
        Robolectric.application.registerReceiver(gcmReceiver, registrationUnregistrationFilter, permissionSend, null);
    }

    @Test
    public void testBroadcastReceiverRegistered() {
        final List<ShadowApplication.Wrapper> registeredReceivers =
                Robolectric.getShadowApplication().getRegisteredReceivers();

        assertFalse(registeredReceivers.isEmpty());

        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (!receiverFound) {
                receiverFound = GCMReceiver.class.getSimpleName()
                        .equals(wrapper.broadcastReceiver.getClass().getSimpleName());
            }
        }

        assertTrue(receiverFound); //will be false if not found
    }

    @Test
    public void testIntentHandlingActionRegistration_WithoutRegistrationId() {
        final ShadowApplication shadowApplication = Robolectric.getShadowApplication();

        final Intent intent = new Intent(ACTION_REGISTRATION);
        final GCMReceiver receiver = getGCMReceiver(intent);
        receiver.onReceive(Robolectric.application, intent);

        final Intent serviceIntent = shadowApplication.peekNextStartedService();
        assertNull(serviceIntent);
    }

    @Test
    public void testIntentHandlingActionRegistration_WithRegistrationId() {
        final ShadowApplication shadowApplication = Robolectric.getShadowApplication();

        final Intent intent = new Intent(ACTION_REGISTRATION);
        intent.putExtra(EXTRA_REGISTRATION_ID, REGISTRATION_ID);
        final GCMReceiver receiver = getGCMReceiver(intent);
        receiver.onReceive(Robolectric.application, intent);

        final Intent serviceIntent = shadowApplication.peekNextStartedService();
        assertEquals("Expected the GCMService service to be invoked",
                GCMService.class.getCanonicalName(),
                serviceIntent.getComponent().getClassName());
        assertEquals(ACTION_REGISTRATION_CALLBACK, serviceIntent.getAction());
        assertEquals(REGISTRATION_ID, serviceIntent.getStringExtra(EXTRA_REGISTRATION_ID));
    }

    @Test
    public void testIntentHandlingReceive() {
        final ShadowApplication shadowApplication = Robolectric.getShadowApplication();

        final String message = "message";
        final Bundle extras = new Bundle();
        extras.putString(MESSAGE_EXTRA_KEY, message);

        final Intent intent = new Intent(ACTION_RECEIVE);
        intent.putExtras(extras);
        final GCMReceiver receiver = getGCMReceiver(intent);
        receiver.onReceive(Robolectric.application, intent);

        final Intent serviceIntent = shadowApplication.peekNextStartedService();
        assertEquals("Expected the GCMService service to be invoked",
                GCMService.class.getCanonicalName(),
                serviceIntent.getComponent().getClassName());
        assertEquals(ACTION_RECEIVE, serviceIntent.getAction());
        final Bundle actualExtras = serviceIntent.getExtras();
        assertFalse(actualExtras.isEmpty());
        final String actualMessage = actualExtras.getString(MESSAGE_EXTRA_KEY);
        assertEquals(message, actualMessage);
    }

    @Test
    public void testIntentHandlingUnregistrationCallback() {
        final ShadowApplication shadowApplication = Robolectric.getShadowApplication();

        final Intent intent = new Intent(ACTION_UNREGISTRATION_CALLBACK);
        intent.putExtra(EXTRA_REGISTRATION_ID, REGISTRATION_ID);
        final GCMReceiver receiver = getGCMReceiver(intent);
        receiver.onReceive(Robolectric.application, intent);

        final Intent serviceIntent = shadowApplication.peekNextStartedService();
        assertEquals("Expected the GCMService service to be invoked",
                GCMService.class.getCanonicalName(),
                serviceIntent.getComponent().getClassName());
        assertEquals(ACTION_UNREGISTRATION_CALLBACK, serviceIntent.getAction());
        assertEquals(REGISTRATION_ID, serviceIntent.getStringExtra(EXTRA_REGISTRATION_ID));
    }

    @After
    public void unregister() {
        Robolectric.application.unregisterReceiver(gcmReceiver);
    }

    private GCMReceiver getGCMReceiver(@NonNull final Intent intent) {
        final ShadowApplication shadowApplication = Robolectric.getShadowApplication();

        assertTrue(shadowApplication.hasReceiverForIntent(intent));

        final List<BroadcastReceiver> receiversForIntent = shadowApplication.getReceiversForIntent(intent);
        assertEquals("Expected one broadcast receiver", 1, receiversForIntent.size());

        return (GCMReceiver) receiversForIntent.get(0);
    }
}
