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
import android.support.annotation.NonNull;

import org.onepf.openpush.exception.OpenPushException;

/**
 * @author Anton Rutkevich
 * @since 19.05.14
 */
public abstract class BasePushProvider implements PushProvider {

    private final Context mContext;

    public BasePushProvider(@NonNull Context context, @NonNull String implementationDependencyClass) {
        checkImplementationClassPresent(implementationDependencyClass);
        mContext = context.getApplicationContext();
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * Checks whether the required class is presented.
     *
     * @param className class to check
     */
    private static void checkImplementationClassPresent(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new OpenPushException(String.format("Class '%s' not present." +
                    "Check you dependencies.", className));
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
