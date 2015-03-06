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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * @author Roman Savin
 * @since 04.03.2015
 */
public final class ManifestUtils {

    public static String getSecurityExceptionMessage(final String permission) {
        return String.format(
                Locale.US,
                "You must add %s permission to the AndroidManifest.xml",
                permission
        );
    }

    public static void checkService(@NonNull final Context context,
                                    @NonNull final ComponentName serviceName) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getServiceInfo(serviceName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("Service " + serviceName
                    + " hasn't been declared in AndroidManifest.xml");
        }
    }

    private ManifestUtils() {
        throw new UnsupportedOperationException();
    }
}
