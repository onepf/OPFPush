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
import org.onepf.opfutils.OPFLog;

/**
 * {@link org.onepf.opfpush.OPFPushHelper} instance holder.
 * <p/>
 * Use {@link #init(android.content.Context, org.onepf.opfpush.configuration.Configuration)} for the
 * initialization of {@link org.onepf.opfpush.OPFPushHelper} global singleton instance.
 * Use {@link #getHelper()} to get initialized {@link org.onepf.opfpush.OPFPushHelper} instance.
 *
 * @author Roman Savin
 * @since 12.01.15
 */
public final class OPFPush {

    @Nullable
    private static OPFPushHelper helper;

    private OPFPush() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@link org.onepf.opfpush.OPFPushHelper} instance.
     *
     * @return The {@link org.onepf.opfpush.OPFPushHelper} instance.
     * @throws org.onepf.opfpush.exception.OPFPushException If {@code OPFPush} wasn't initialized.
     */
    @NonNull
    public static OPFPushHelper getHelper() {
        OPFLog.methodD();
        if (helper == null) {
            throw new OPFPushException("OPFPush doesn't init");
        }
        return helper;
    }

    /**
     * Initializes the OPFPush library and creates {@link org.onepf.opfpush.OPFPushHelper} singleton instance.
     *
     * @param context       {@link android.content.Context} instance.
     * @param configuration {@link org.onepf.opfpush.configuration.Configuration} instance.
     * @throws org.onepf.opfutils.exception.WrongThreadException If this method is not called from the main thread.
     * @throws org.onepf.opfutils.exception.InitException        If {@code OPFPush} has already been initialized.
     */
    public static void init(@NonNull final Context context,
                            @NonNull final Configuration configuration) {
        OPFLog.methodD(context, configuration);
        OPFChecks.checkThread(true);
        if (helper == null) {
            helper = new OPFPushHelper(context);
        }
        helper.checkInit(false);
        helper.init(configuration);
    }
}
