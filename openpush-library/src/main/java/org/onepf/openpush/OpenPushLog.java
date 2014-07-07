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

import android.content.Intent;
import android.os.Bundle;

/**
 * @author Anton Rutkevich
 * @since 14.05.14
 */
public class OpenPushLog {

    private static boolean enabled = true;

    /**
     * Enables the logging.
     */
    public static void enable() {
        enabled = true;
    }

    /**
     * Disables the logging.
     */
    public static void disable() {
        enabled = false;
    }

    /**
     * Is the logging enabled?
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the detailed description of the intent.
     */
    public static String intentToString(Intent intent) {
        return "Action: " + intent.getAction() + ", extras: " + bundleToString(intent.getExtras());
    }

    /**
     * Returnts the detailed bundle description.
     */
    public static String bundleToString(Bundle bundle) {
        if (bundle != null) {
            // Implicitly calls unparcel
            bundle.isEmpty();
            return bundle.toString();
        }

        return null;
    }
}
