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

import android.util.Log;

//CHECKSTYLE:OFF
public final class OPFPushLog {

    private static final String TAG = "OpenPush";

    private static boolean sLogEnable;

    public static boolean isLogEnable() {
        return sLogEnable;
    }

    public static void setLogEnable(boolean logEnable) {
        sLogEnable = logEnable;
    }

    public static void LOGD(String message) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (sLogEnable || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, message);
        }
    }

    public static void LOGD(String messageFormat, Object... args) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (sLogEnable || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, String.format(messageFormat, args));
        }
    }

    public static void LOGD(String message, Throwable cause) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (sLogEnable || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, message, cause);
        }
    }

    public static void LOGV(String message) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (sLogEnable && BuildConfig.DEBUG && Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, message);
        }
    }

    public static void LOGV(String message, Throwable cause) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (sLogEnable && BuildConfig.DEBUG && Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, message, cause);
        }
    }

    public static void LOGI(String message) {
        if (sLogEnable) {
            Log.i(TAG, message);
        }
    }

    public static void LOGI(String messageFormat, Object... args) {
        if (sLogEnable) {
            Log.i(TAG, String.format(messageFormat, args));
        }
    }

    public static void LOGI(String message, Throwable cause) {
        if (sLogEnable) {
            Log.i(TAG, message, cause);
        }
    }

    public static void LOGW(String message) {
        if (sLogEnable) {
            Log.w(TAG, message);
        }
    }

    public static void LOGW(String message, Throwable cause) {
        if (sLogEnable) {
            Log.w(TAG, message, cause);
        }
    }

    public static void LOGW(String message, Object... args) {
        if (sLogEnable) {
            Log.w(TAG, String.format(message, args));
        }
    }

    public static void LOGE(String message) {
        if (sLogEnable) {
            Log.e(TAG, message);
        }
    }

    public static void LOGE(String message, Throwable cause) {
        if (sLogEnable) {
            Log.e(TAG, message, cause);
        }
    }

    private OPFPushLog() {
    }
}
//CHECKSTYLE:ON

