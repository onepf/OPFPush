/*******************************************************************************
 * Copyright 2014 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

package org.onepf.openpush;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

/**
 * @author Kirill Rozov
 * @since 05.09.14
 */
public abstract class BasePushProvider implements PushProvider {

    private final Context mContext;
    private final String mImplementationDependencyClass;

    public BasePushProvider(@NotNull Context context, @NotNull String implementationDependencyClass) {
        mContext = context.getApplicationContext();
        mImplementationDependencyClass = implementationDependencyClass;
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName(mImplementationDependencyClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return getName();
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
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public void close() {
    }

    @Override
    public void onAppStateChanged() {
    }
}
