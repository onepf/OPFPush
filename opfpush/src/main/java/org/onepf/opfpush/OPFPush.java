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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfutils.OPFChecks;

/**
 * @author Roman Savin
 * @since 12.01.15
 */
public final class OPFPush {

    @Nullable
    private static OPFPushHelper helper;

    private OPFPush() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static OPFPushHelper getHelper() {
        OPFPushLog.methodD(OPFPush.class, "getHelper");
        if (helper == null) {
            throw new OPFPushException("OPFPush doesn't init");
        }
        return helper;
    }

    public static void init(@NonNull final Context context,
                            @NonNull final Configuration configuration) {
        OPFPushLog.methodD(OPFPush.class, "init", context, configuration);
        OPFChecks.checkThread(true);
        if (helper == null) {
            helper = new OPFPushHelper(context);
        }
        helper.init(configuration);
    }
}
