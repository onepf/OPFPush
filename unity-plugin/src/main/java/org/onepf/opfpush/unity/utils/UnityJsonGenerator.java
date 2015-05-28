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

package org.onepf.opfpush.unity.utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonWriter;
import org.onepf.opfpush.model.UnrecoverablePushError;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Create JSONs which are sent to Unity using {@link com.unity3d.player.UnityPlayer#UnitySendMessage(String, String, String)}
 *
 * @author Roman Savin
 * @since 20.05.2015
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public final class UnityJsonGenerator {

    private static final String PROVIDER_NAME_FIELD = "providerName";
    private static final String REGISTRATION_ID_FIELD = "registrationId";
    private static final String OLD_REGISTRATION_ID_FIELD = "oldRegistrationId";
    private static final String MESSAGES_COUNT_FIELD = "messagesCount";
    private static final String MESSAGE_DATA_FIELD = "data";
    private static final String PUSH_ERROR_FIELD = "pushErrors";
    private static final String AVAILABILITY_ERROR_CODE_FIELD = "availabilityErrorCode";
    private static final String TYPE_FIELD = "type";
    private static final String ORIGINAL_ERROR_FIELD = "originalError";

    private UnityJsonGenerator() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static String getOnRegisteredJson(@NonNull final String providerName,
                                             @NonNull final String registrationId) throws IOException {
        final StringWriter writer = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(writer);
        try {
            jsonWriter.beginObject();
            jsonWriter.name(PROVIDER_NAME_FIELD).value(providerName);
            jsonWriter.name(REGISTRATION_ID_FIELD).value(registrationId);
            jsonWriter.endObject();
        } finally {
            jsonWriter.close();
        }
        return writer.toString();
    }

    @NonNull
    public static String getOnUnregisteredJson(@NonNull final String providerName,
                                               @Nullable final String oldRegistrationId) throws IOException {
        final StringWriter writer = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(writer);
        try {
            jsonWriter.beginObject();
            jsonWriter.name(PROVIDER_NAME_FIELD).value(providerName);
            jsonWriter.name(OLD_REGISTRATION_ID_FIELD).value(oldRegistrationId);
            jsonWriter.endObject();
        } finally {
            jsonWriter.close();
        }
        return writer.toString();
    }

    @NonNull
    public static String getOnDeletedJson(@NonNull final String providerName,
                                          final int messagesCount) throws IOException {
        final StringWriter writer = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(writer);
        try {
            jsonWriter.beginObject();
            jsonWriter.name(PROVIDER_NAME_FIELD).value(providerName);
            jsonWriter.name(MESSAGES_COUNT_FIELD).value(messagesCount);
            jsonWriter.endObject();
        } finally {
            jsonWriter.close();
        }
        return writer.toString();
    }

    @NonNull
    public static String getOnMessageJson(@NonNull final String providerName,
                                          @Nullable final Bundle bundle) throws IOException {
        final StringWriter writer = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(writer);
        try {
            jsonWriter.beginObject();
            jsonWriter.name(PROVIDER_NAME_FIELD).value(providerName);
            if (bundle != null) {
                writeExtras(jsonWriter, bundle);
            }
            jsonWriter.endObject();
        } finally {
            jsonWriter.close();
        }

        return writer.toString();
    }

    public static String getOnNoAvailableProviderJson(
            @NonNull final Map<String, UnrecoverablePushError> pushErrors
    ) throws IOException {

        final StringWriter writer = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(writer);
        try {
            jsonWriter.beginObject();
            jsonWriter.name(PUSH_ERROR_FIELD);
            jsonWriter.beginObject();
            for (String providerName : pushErrors.keySet()) {
                jsonWriter.name(providerName);
                writeUnrecoverablePushError(jsonWriter, pushErrors.get(providerName));
            }
            jsonWriter.endObject();
            jsonWriter.endObject();
        } finally {
            jsonWriter.close();
        }

        return writer.toString();
    }

    private static void writeExtras(@NonNull final JsonWriter jsonWriter,
                                    @NonNull final Bundle bundle) throws IOException {
        jsonWriter.name(MESSAGE_DATA_FIELD);
        jsonWriter.beginObject();
        for (final String key : bundle.keySet()) {
            jsonWriter.name(key).value(bundle.getString(key));
        }
        jsonWriter.endObject();
    }

    private static void writeUnrecoverablePushError(@NonNull final JsonWriter jsonWriter,
                                                    @NonNull final UnrecoverablePushError pushError) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name(AVAILABILITY_ERROR_CODE_FIELD).value(pushError.getAvailabilityErrorCode());
        jsonWriter.name(TYPE_FIELD).value(pushError.getType().toString());
        jsonWriter.name(ORIGINAL_ERROR_FIELD).value(pushError.getOriginalError());
        jsonWriter.endObject();
    }
}
