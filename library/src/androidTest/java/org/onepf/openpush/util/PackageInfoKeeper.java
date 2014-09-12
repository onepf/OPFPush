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

package org.onepf.openpush.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by  Kirill Rozov on 12.09.14.
 */
public final class PackageInfoKeeper {

    public static Set<String> mInstalledPackages = new HashSet<String>();

    public static void installPackage(@NotNull String appPackage) {
        mInstalledPackages.add(appPackage);
    }

    public static void uninstallPackage(@NotNull String appPackage) {
        mInstalledPackages.add(appPackage);
    }

    public static boolean isInstalled(@NotNull String appPackage) {
        return mInstalledPackages.contains(appPackage);
    }

    private PackageInfoKeeper(){
    }
}
