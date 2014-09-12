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

package org.onepf.openpush.shadow;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.jetbrains.annotations.NotNull;
import org.onepf.openpush.util.PackageInfoKeeper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kirill Rozov on 12.09.14.
 */
@Implements(value = PackageManager.class,
        className = "android.app.ApplicationPackageManager")
public class ShadowPackageManager {

    @RealObject
    PackageManager mPackageManager;

    @Implementation
    public ApplicationInfo getApplicationInfo(String packageName,
                                              int flags)
            throws PackageManager.NameNotFoundException {

        return PackageInfoKeeper.isInstalled(packageName) ?
                mPackageManager.getApplicationInfo(packageName, flags) : null;
    }
}
