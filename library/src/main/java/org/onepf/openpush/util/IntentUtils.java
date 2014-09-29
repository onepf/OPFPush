package org.onepf.openpush.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by krozov on 29.09.14.
 */
public final class IntentUtils {

    public static String extrasToString(@NonNull Intent intent) {
        if (intent.getExtras().isEmpty()) {
            return "";
        }

        Bundle extras = intent.getExtras();
        StringBuilder builder = new StringBuilder("[");
        for (String key : extras.keySet()) {
            builder.append('"')
                    .append(key)
                    .append('"')
                    .append(':')
                    .append('"')
                    .append(extras.get(key))
                    .append('"')
                    .append(", ");
        }
        builder.append("]");
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    private IntentUtils() {
    }
}
