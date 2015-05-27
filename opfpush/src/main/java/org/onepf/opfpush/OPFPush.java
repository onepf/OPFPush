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

import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;
import org.onepf.opfutils.exception.InitException;

/**
 * The {@link org.onepf.opfpush.OPFPushHelper} instance holder.
 * <p/>
 * Use the {@link #init(android.content.Context, org.onepf.opfpush.configuration.Configuration)} method to
 * initialize the {@link org.onepf.opfpush.OPFPushHelper} global singleton instance.
 * Use the {@link #getHelper()} to get the initialized {@link org.onepf.opfpush.OPFPushHelper} instance.
 *
 * @author Roman Savin
 * @since 12.01.15
 */
public final class OPFPush {

    private static volatile OPFPushHelper helper;

    private OPFPush() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@link org.onepf.opfpush.OPFPushHelper} instance.
     *
     * @return The {@link org.onepf.opfpush.OPFPushHelper} instance.
     * @throws org.onepf.opfutils.exception.InitException If {@code OPFPush} wasn't initialized.
     */
    @NonNull
    public static OPFPushHelper getHelper() {
        OPFLog.logMethod();
        if (helper == null) {
            throw new InitException(false);
        }
        return helper;
    }

    /**
     * Initializes the OPFPush library and creates the {@link org.onepf.opfpush.OPFPushHelper} singleton instance.
     *
     * @param context       The {@link android.content.Context} instance.
     * @param configuration The {@link org.onepf.opfpush.configuration.Configuration} instance.
     * @throws org.onepf.opfutils.exception.WrongThreadException If this method is not called from the main thread.
     * @throws org.onepf.opfutils.exception.InitException        If the {@code OPFPush} has already been initialized.
     */
    public static void init(@NonNull final Context context,
                            @NonNull final Configuration configuration) {
        OPFLog.logMethod(context, configuration);
        OPFChecks.checkThread(true);

        if (helper != null) {
            throw new InitException(true);
        }

        if (OPFUtils.isMainProcess(context)) {
            OPFLog.i("Init in main process");
            final OPFPushHelper newHelper = new OPFPushHelperImpl(context);
            newHelper.init(configuration);
            helper = newHelper;
        } else {
            OPFLog.i("Init in not main process");
            helper = new OPFPushHelperStub();
        }
    }
}
