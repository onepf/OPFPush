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

package org.onepf.openpush.gcm;

import android.support.annotation.StringDef;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by krozov on 06.09.14.
 */
class GCMConstants {
    static final String ACTION_REGISTRATION = "org.onepf.openpush.gcm.registration";
    static final String ACTION_UNREGISTRATION = "org.onepf.openpush.gcm.unregistration";

    static final String EXTRA_ERROR_ID = "org.onepf.openpush.gcm.error_id";
    static final String EXTRA_TOKEN = "org.onepf.openpush.gcm.token";

    static final String ERROR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    static final String ERROR_SERVICE_NOT_AVAILABLE = GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE;

    /**
     * Created by krozov on 24.09.14.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ERROR_AUTHENTICATION_FAILED,
            ERROR_SERVICE_NOT_AVAILABLE
    })
    public static @interface GCMError {
    }
    /**
     * Created by krozov on 24.09.14.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            GCMConstants.ACTION_REGISTRATION,
            GCMConstants.ACTION_UNREGISTRATION
    })
    public static @interface GCMAction {
    }
}
