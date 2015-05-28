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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.adm.ADMProvider;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.gcm.GCMProvider;
import org.onepf.opfpush.nokia.NokiaNotificationsProvider;
import org.onepf.opfpush.unity.listener.PushEventListener;
import org.onepf.opfutils.OPFLog;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Roman Savin
 * @since 19.05.2015
 */
public final class OPFPushInitializer {

    private static final String OPFPUSH_CONFIG_FILE_NAME = "opfpush_config.json";

    private static final String PROVIDERS_FIELD = "providers";
    private static final String DEBUG_FIELD = "debug";
    private static final String LOG_ENABLED_FIELD = "logEnabled";
    private static final String SELECT_SYSTEM_PREFERRED_FIELD = "selectSystemPreferred";

    private static final String NAME_FIELD = "name";
    private static final String SENDER_ID_FIELD = "senderId";
    private static final String SENDER_IDS_ARRAY_FIELD = "senderIdsArray";

    private static final String GCM_PROVIDER_NAME = "gcm";
    private static final String ADM_PROVIDER_NAME = "adm";
    private static final String NOKIA_PROVIDER_NAME = "nokia";

    private static final String TAG = OPFPushInitializer.class.getSimpleName();

    private OPFPushInitializer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initialize OPFPush using opfpush_config.json which must place in assets.
     */
    public static void init(@NonNull final Context context) {
        try {
            OPFPush.init(context, readConfiguration(context));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static Configuration readConfiguration(@NonNull final Context context) throws IOException {
        final Configuration.Builder configBuilder = new Configuration.Builder();
        boolean isDebug = false;
        boolean isLogEnabled = false;

        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new InputStreamReader(
                    context.getAssets().open(OPFPUSH_CONFIG_FILE_NAME),
                    "UTF-8"
            ));

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                final String name = jsonReader.nextName();
                switch (name) {
                    case PROVIDERS_FIELD:
                        readProviders(context, configBuilder, jsonReader);
                        break;
                    case DEBUG_FIELD:
                        isDebug = jsonReader.nextBoolean();
                        break;
                    case LOG_ENABLED_FIELD:
                        isLogEnabled = jsonReader.nextBoolean();
                        break;
                    case SELECT_SYSTEM_PREFERRED_FIELD:
                        configBuilder.setSelectSystemPreferred(jsonReader.nextBoolean());
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }
            jsonReader.endObject();
        } finally {
            if (jsonReader != null) {
                jsonReader.close();
            }
        }
        OPFLog.setEnabled(isDebug, isLogEnabled);

        configBuilder.setEventListener(new PushEventListener());
        return configBuilder.build();
    }

    private static void readProviders(@NonNull final Context context,
                                      @NonNull final Configuration.Builder configBuilder,
                                      @NonNull final JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            readProvider(context, configBuilder, jsonReader);
        }
        jsonReader.endArray();
    }

    private static void readProvider(@NonNull final Context context,
                                     @NonNull final Configuration.Builder configBuilder,
                                     @NonNull final JsonReader jsonReader) throws IOException {
        String providerName = null;
        final List<String> senderIds = new ArrayList<>();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            final String name = jsonReader.nextName();
            switch (name) {
                case NAME_FIELD:
                    providerName = jsonReader.nextString();
                    break;
                case SENDER_ID_FIELD:
                    senderIds.add(jsonReader.nextString());
                    break;
                case SENDER_IDS_ARRAY_FIELD:
                    readSenderIdArray(senderIds, jsonReader);
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        addProvider(context, configBuilder, providerName, senderIds);
    }

    private static void readSenderIdArray(@NonNull final List<String> senderIds,
                                          @NonNull JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            senderIds.add(jsonReader.nextString());
        }
        jsonReader.endArray();
    }

    private static void addProvider(@NonNull final Context context,
                                    @NonNull final Configuration.Builder configBuilder,
                                    @Nullable final String providerName,
                                    @NonNull final List<String> senderIds) {

        if (providerName != null) {
            final String addedProviderName = providerName.toLowerCase(Locale.US);
            switch (addedProviderName) {
                case GCM_PROVIDER_NAME:
                    configBuilder.addProviders(new GCMProvider(context, safeGetFirstSenderId(senderIds)));
                    break;
                case ADM_PROVIDER_NAME:
                    configBuilder.addProviders(new ADMProvider(context));
                    break;
                case NOKIA_PROVIDER_NAME:
                    configBuilder.addProviders(new NokiaNotificationsProvider(context,
                            senderIds.toArray(new String[senderIds.size()])));
                    break;
            }
        }
    }

    @NonNull
    private static String safeGetFirstSenderId(@NonNull final List<String> senderIds) {
        if (senderIds.isEmpty()) {
            return "";
        }

        return senderIds.get(0);
    }
}
