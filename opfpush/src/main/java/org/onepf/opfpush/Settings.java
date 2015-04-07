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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.model.State;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFPreferences;

import java.util.Locale;

import static org.onepf.opfpush.model.State.UNREGISTERED;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 01.10.14.
 */
@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
final class Settings {

    private static final String KEY_LAST_PROVIDER_NAME = "last_provider_name";
    private static final String KEY_STATE = "state";
    private static final String KEY_LAST_ANDROID_ID = "android_id";
    private static final String KEY_UNREGISTERING_PROVIDER_PREFIX = "unregistering_provider_";
    private static final String KEY_REGISTERING_PROVIDER_PREFIX = "registering_provider_";
    private static final String KEY_PENDING_REGISTRATION_PROVIDER = "pending_registration_provider";
    private static final String KEY_PENDING_UNREGISTRATION_PROVIDER = "pending_unregistration_provider";

    private static volatile Settings instance;

    @NonNull
    private final OPFPreferences preferences;

    private Settings(@NonNull final Context context) {
        preferences = new OPFPreferences(context);
    }

    @SuppressWarnings("PMD.NonThreadSafeSingleton")
    public static Settings getInstance(@NonNull final Context context) {
        OPFChecks.checkThread(true);
        if (instance == null) {
            instance = new Settings(context);
        }

        return instance;
    }

    @NonNull
    public synchronized State getState() {
        OPFLog.logMethod();

        final int stateValue = preferences.getInt(KEY_STATE, UNREGISTERED.getValue());
        State state = State.fromValue(stateValue);

        OPFLog.d("State : " + state);
        if (state == null) {
            state = UNREGISTERED;
            saveState(state);
        }
        return state;
    }

    public synchronized void saveState(@NonNull final State state) {
        OPFLog.logMethod(state);
        preferences.put(KEY_STATE, state.getValue());
    }

    public synchronized void clear() {
        OPFLog.logMethod();
        preferences.clear();
    }

    @Nullable
    public synchronized String getLastProviderName() {
        return preferences.getString(KEY_LAST_PROVIDER_NAME);
    }

    public synchronized void saveLastProvider(@Nullable final PushProvider provider) {
        OPFLog.logMethod(provider);

        if (provider == null) {
            preferences.remove(KEY_LAST_PROVIDER_NAME);
        } else {
            preferences.put(KEY_LAST_PROVIDER_NAME, provider.getName());
        }
    }

    @Nullable
    public synchronized String getLastAndroidId() {
        return preferences.getString(KEY_LAST_ANDROID_ID);
    }

    public synchronized void saveLastAndroidId(@Nullable final String androidId) {
        OPFLog.logMethod(androidId);

        if (androidId == null) {
            preferences.remove(KEY_LAST_ANDROID_ID);
        } else {
            preferences.put(KEY_LAST_ANDROID_ID, androidId);
        }
    }

    public synchronized void saveUnregisteringProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        preferences.put(
                getProviderPreferenceKey(KEY_UNREGISTERING_PROVIDER_PREFIX, providerName),
                true
        );
    }

    public synchronized void removeUnregisteringProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        preferences.remove(getProviderPreferenceKey(KEY_UNREGISTERING_PROVIDER_PREFIX, providerName));
    }

    public synchronized boolean isProviderUnregistrationPerforming(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        return preferences.getBoolean(
                getProviderPreferenceKey(KEY_UNREGISTERING_PROVIDER_PREFIX, providerName),
                false
        );
    }

    public synchronized void saveRegisteringProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        preferences.put(
                getProviderPreferenceKey(KEY_REGISTERING_PROVIDER_PREFIX, providerName),
                true
        );
    }

    public synchronized void removeRegisteringProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        preferences.remove(getProviderPreferenceKey(KEY_REGISTERING_PROVIDER_PREFIX, providerName));
    }

    public synchronized boolean isProviderRegistrationPerforming(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        return preferences.getBoolean(
                getProviderPreferenceKey(KEY_REGISTERING_PROVIDER_PREFIX, providerName),
                false
        );
    }

    public synchronized void savePendingRegistrationProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        preferences.put(KEY_PENDING_REGISTRATION_PROVIDER, providerName);
    }

    public synchronized void removePendingRegistrationProvider() {
        OPFLog.logMethod();
        preferences.remove(KEY_PENDING_REGISTRATION_PROVIDER);
    }

    @Nullable
    public synchronized String getPendingRegistrationProvider() {
        OPFLog.logMethod();
        return preferences.getString(KEY_PENDING_REGISTRATION_PROVIDER);
    }

    public synchronized void savePendingUnregistrationProvider(@NonNull final String providerName) {
        OPFLog.logMethod(providerName);
        preferences.put(KEY_PENDING_UNREGISTRATION_PROVIDER, providerName);
    }

    public synchronized void removePendingUnregistrationProvider() {
        OPFLog.logMethod();
        preferences.remove(KEY_PENDING_UNREGISTRATION_PROVIDER);
    }

    @Nullable
    public synchronized String getPendingUnregistrationProvider() {
        OPFLog.logMethod();
        return preferences.getString(KEY_PENDING_UNREGISTRATION_PROVIDER);
    }

    private String getProviderPreferenceKey(@NonNull final String prefix,
                                            @NonNull final String providerName) {
        return prefix + providerName.toLowerCase(Locale.US);
    }
}
