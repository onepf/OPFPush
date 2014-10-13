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
 * @since 29.09.14
 */
public final class Utils {

    public static final String ITEM_DIVIDER = ", ";

    /**
     * Convert intent to string.
     *
     * @return String representation of intent.
     */
    @Nullable
    public static String toString(@NonNull Intent intent) {
        StringBuilder b = new StringBuilder();
        b.append("Intent{");
        b.append("action=").append('"').append(intent.getAction()).append('"');
        b.append(ITEM_DIVIDER);
        b.append("data=").append('"').append(intent.getDataString()).append('"');
        b.append(ITEM_DIVIDER);
        b.append("component=").append('"').append(intent.getComponent()).append('"');
        b.append(ITEM_DIVIDER);
        Bundle extras = intent.getExtras();
        b.append("extras=").append(extras == null ? null : toString(extras));
        b.append('}');
        return b.toString();
    }

    /**
     * Convert {@code Bundle} to string.
     *
     * @return String representation of bundles.
     */
    @NonNull
    public static String toString(@NonNull Bundle bundle) {
        if (bundle.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (String key : bundle.keySet()) {
            builder.append('"').append(key).append('"');
            builder.append(':');
            builder.append('"').append(bundle.get(key)).append('"');
            builder.append(ITEM_DIVIDER);
        }
        builder.setLength(builder.length() - ITEM_DIVIDER.length());
        builder.append(']');
        return builder.toString();
    }

    public static boolean isNetworkConnected(@NonNull Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Utils() {
    }
}
