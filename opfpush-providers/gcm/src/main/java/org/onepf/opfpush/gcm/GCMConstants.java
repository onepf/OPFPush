/*
 * Copyright 2012-2015 One Platform Foundation
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

package org.onepf.opfpush.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This class contains the {@code GCMProvider} specific constants.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 06.09.14.
 */
public final class GCMConstants {

    /**
     * The name of the {@code GCMProvider}
     */
    public static final String PROVIDER_NAME = "Google Cloud Messaging";

    static final String C2DM_ACTION_RECEIVE = "com.google.android.c2dm.intent.RECEIVE";

    static final String ERROR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    static final String ERROR_SERVICE_NOT_AVAILABLE = GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE;

    static final String GOOGLE_PLAY_APP_PACKAGE = "com.android.vending";

    static final String PERMISSION_RECEIVE = "com.google.android.c2dm.permission.RECEIVE";
    static final String PERMISSION_C2D_MESSAGE_SUFFIX = ".permission.C2D_MESSAGE";
    static final String GOOGLE_CLOUD_MESSAGING_CLASS_NAME = "com.google.android.gms.gcm.GoogleCloudMessaging";
    static final String MESSAGES_TO_SUFFIX = "@gcm.googleapis.com";

    static final String GOOGLE_SERVICES_FRAMEWORK_PACKAGE = "com.google.android.gsf";

    static final String GCM_NOT_CURRENT_PROVIDER_ERROR = "GCM is no current provider";
    static final String REGISTRATION_ID_NOT_OBTAINED_ERROR = "Registration token isn't obtained";

    private GCMConstants() {
        throw new UnsupportedOperationException();
    }
}
