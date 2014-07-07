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

import android.util.Log;

/**
 * @author Anton Rutkevich
 * @since 19.05.14
 */
public abstract class BasePushProvider implements PushProvider {

    private static final String TAG = "OpenPush";

    public BasePushProvider(String implementationDependencyClass) {
        checkDependencies(implementationDependencyClass);
    }

    private static boolean checkDependencies(String provider) {
        return isImplementationClassPresent(provider);
    }

    /**
     * Checks whether the required class is presented.
     * @param className class to check
     * @return is the class available
     */
    private static boolean isImplementationClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            processError("Class is not in classpath: " + className, e);
            return false;
        }
    }

    /**
     * Process the error depending on the current mode.
     * @param errorMessage message to process
     * @param cause cause to process
     */
    private static void processError(String errorMessage, Throwable cause) {
        if (OpenPushStrictMode.isEnabled()) {
            if (cause != null) {
                throw new OpenPushException(errorMessage, cause);
            } else {
                throw new OpenPushException(errorMessage);
            }
        }
        if (OpenPushLog.isEnabled()) {
            Log.w(TAG, errorMessage);
        }
    }

}
