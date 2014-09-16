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

import org.jetbrains.annotations.NotNull;
import org.onepf.openpush.util.PackageUtils;

/**
 * @author Kirill Rozov
 * @since 05.09.14
 */
public abstract class BasePushProvider implements PushProvider {

    private final Context mAppContext;
    private final String mName;
    private final String mHostAppPackage;

    protected BasePushProvider(@NotNull Context context,
                               @NotNull String name,
                               @NotNull String hostAppPackage) {
        mAppContext = context.getApplicationContext();
        mName = name;
        mHostAppPackage = hostAppPackage;

        if (!checkManifest()) {
            throw new OpenPushException("Your manifest doesn't contain all required permissions." +
                    " Check your AndroidManifest.xml.");
        }
    }

    @NotNull
    protected Context getContext() {
        return mAppContext;
    }

    @Override
    public boolean isAvailable() {
        return PackageUtils.isInstalled(mAppContext, getHostAppPackage());
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
        return PackageUtils.checkPermission(mAppContext, android.Manifest.permission.INTERNET);
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @NotNull
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

    @NotNull
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
