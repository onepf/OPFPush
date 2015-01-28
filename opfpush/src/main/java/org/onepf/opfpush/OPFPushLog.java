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

package org.onepf.opfpush;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @author Roman Savin
 * @since 11.12.14
 */
public final class OPFPushLog {

    private static final String TAG = "OPFPush";

    private static final String ITEM_DIVIDER = ", ";

    private static boolean isLogEnable;

    private OPFPushLog() {
        throw new UnsupportedOperationException();
    }

    public static boolean isLogEnable() {
        return isLogEnable;
    }

    public static void setLogEnable(final boolean isLogEnable) {
        OPFPushLog.isLogEnable = isLogEnable;
    }

    public static void d(@Nullable final String message) {
        if (isLogEnable || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, message);
        }
    }

    public static void d(@NonNull final String messageFormat, @Nullable final Object... args) {
        if (isLogEnable || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, String.format(messageFormat, args));
        }
    }

    public static void d(@Nullable final String message, @Nullable final Throwable cause) {
        if (isLogEnable || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, message, cause);
        }
    }

    public static void v(@Nullable final String message) {
        if (isLogEnable && BuildConfig.DEBUG && Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, message);
        }
    }

    public static void v(@Nullable final String message, @Nullable final Throwable cause) {
        if (isLogEnable && BuildConfig.DEBUG && Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, message, cause);
        }
    }

    public static void i(@Nullable final String message) {
        if (isLogEnable) {
            Log.i(TAG, message);
        }
    }

    public static void i(@NonNull final String messageFormat, @Nullable final Object... args) {
        if (isLogEnable) {
            Log.i(TAG, String.format(messageFormat, args));
        }
    }

    public static void i(@Nullable final String message, @Nullable final Throwable cause) {
        if (isLogEnable) {
            Log.i(TAG, message, cause);
        }
    }

    public static void w(@Nullable final String message) {
        if (isLogEnable) {
            Log.w(TAG, message);
        }
    }

    public static void w(@Nullable final String message, @Nullable final Throwable cause) {
        if (isLogEnable) {
            Log.w(TAG, message, cause);
        }
    }

    public static void w(@NonNull final String message, @Nullable final Object... args) {
        if (isLogEnable) {
            Log.w(TAG, String.format(message, args));
        }
    }

    public static void e(@Nullable final String message) {
        Log.e(TAG, message);
    }

    public static void e(@Nullable final String message, @Nullable final Throwable cause) {
        Log.e(TAG, message, cause);
    }

    public static void methodD(@NonNull final Class aClass,
                               @NonNull final String methodName,
                               @Nullable final Object... args) {
        final String logMessage = createMethodLogMessage(aClass, methodName, args);
        d(logMessage);
    }

    public static void methodI(@NonNull final Class aClass,
                               @NonNull final String methodName,
                               @Nullable final Object... args) {
        final String logMessage = createMethodLogMessage(aClass, methodName, args);
        i(logMessage);
    }

    public static void methodW(@NonNull final Class aClass,
                               @NonNull final String methodName,
                               @Nullable final Object... args) {
        final String logMessage = createMethodLogMessage(aClass, methodName, args);
        w(logMessage);
    }

    public static void methodV(@NonNull final Class aClass,
                               @NonNull final String methodName,
                               @Nullable final Object... args) {
        final String logMessage = createMethodLogMessage(aClass, methodName, args);
        v(logMessage);
    }

    public static void methodE(@NonNull final Class aClass,
                               @NonNull final String methodName,
                               @Nullable final Object... args) {
        final String logMessage = createMethodLogMessage(aClass, methodName, args);
        e(logMessage);
    }

    private static String createMethodLogMessage(@NonNull final Class aClass,
                                                 @NonNull final String methodName,
                                                 @Nullable final Object... args) {
        final StringBuilder stringBuilder = new StringBuilder()
                .append(aClass.getSimpleName())
                .append(".")
                .append(methodName)
                .append("(");

        if (args != null) {
            final int length = args.length;
            for (int i = 0; i < length; i++) {
                stringBuilder.append(args[i]);
                if (i != length - 1) {
                    stringBuilder.append(ITEM_DIVIDER);
                }
            }
        }

        stringBuilder.append(");");
        return stringBuilder.toString();
    }
}

