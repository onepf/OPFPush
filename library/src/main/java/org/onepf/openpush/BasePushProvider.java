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

package org.onepf.openpush;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import org.onepf.openpush.util.PackageUtils;

/**
 * Base class for create {@code PushProvider}.
 *
 * @author Kirill Rozov
 * @since 05.09.14
 */
public abstract class BasePushProvider implements PushProvider {

    @NonNull
    private final Context mAppContext;

    @NonNull
    private final String mName;

    @NonNull
    private final String mHostAppPackage;

    /**
     * Base constructor for subclass.
     *
     * @param context        Any instance of {@code Context}.
     * @param name           Provider name.
     * @param hostAppPackage Package of application that handle push message from server
     *                       and deliver it to applications.
     */
    protected BasePushProvider(@NonNull Context context,
                               @NonNull String name,
                               @NonNull String hostAppPackage) {
        mAppContext = context.getApplicationContext();
        mName = name;
        mHostAppPackage = hostAppPackage;

        checkManifest();
    }

    /**
     * Verify is manifest contains permission.
     *
     * @param ctx        Any instance of {@code Context}.
     * @param permission Permission for verify.
     * @return
     */
    protected static boolean checkPermission(@NonNull Context ctx, @NonNull String permission) {
        switch (ctx.getPackageManager().checkPermission(permission, ctx.getPackageName())) {
            case PackageManager.PERMISSION_GRANTED:
                return true;

            default:
                throw new OpenPushException("Your manifest doesn't contain permission '"
                        + permission + ".' Check your AndroidManifest.xml.");
        }
    }

    /**
     * Get {@code Context} instance.
     *
     * @return Instance of {@link Context}.
     */
    @NonNull
    protected Context getContext() {
        return mAppContext;
    }

    @Override
    public boolean isAvailable() {
        return PackageUtils.isInstalled(mAppContext, mHostAppPackage);
    }

    @Override
    public String toString() {
        return mName + "(hostAppPackage='" + mHostAppPackage + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BasePushProvider that = (BasePushProvider) o;
        return mName.equals(that.mName);
    }

    @Override
    public boolean checkManifest() {
        return checkPermission(mAppContext, android.Manifest.permission.INTERNET);
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void close() {
    }

    @Override
    public void onUnavailable() {
    }

    @NonNull
    @Override
    public String getHostAppPackage() {
        return mHostAppPackage;
    }

    @Override
    public void onAppStateChanged() {
    }

    @Override
    public boolean isRegistered() {
        return getRegistrationId() != null;
    }
}
