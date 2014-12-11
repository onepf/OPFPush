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

package org.onepf.opfpush.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Different utils.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 29.09.14
 */
public final class Utils {

    public static final String ITEM_DIVIDER = ", ";

    private Utils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert intent to string.
     *
     * @return String representation of intent.
     */
    @Nullable
    public static String toString(@Nullable final Intent intent) {
        if (intent == null) {
            return "null";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Intent{");
        stringBuilder.append("action=").append('"').append(intent.getAction()).append('"');
        stringBuilder.append(ITEM_DIVIDER);
        stringBuilder.append("data=").append('"').append(intent.getDataString()).append('"');
        stringBuilder.append(ITEM_DIVIDER);
        stringBuilder.append("component=").append('"').append(intent.getComponent()).append('"');
        stringBuilder.append(ITEM_DIVIDER);

        final Bundle extras = intent.getExtras();
        stringBuilder.append("extras=").append(extras == null ? null : toString(extras));
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    /**
     * Convert {@code Bundle} to string.
     *
     * @return String representation of bundles.
     */
    @NonNull
    public static String toString(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return "null";
        }

        if (bundle.isEmpty()) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (String key : bundle.keySet()) {
            stringBuilder.append('"').append(key).append('"');
            stringBuilder.append(':');
            stringBuilder.append('"').append(bundle.get(key)).append('"');
            stringBuilder.append(ITEM_DIVIDER);
        }
        stringBuilder.setLength(stringBuilder.length() - ITEM_DIVIDER.length());
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public static boolean isNetworkConnected(@NonNull final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
