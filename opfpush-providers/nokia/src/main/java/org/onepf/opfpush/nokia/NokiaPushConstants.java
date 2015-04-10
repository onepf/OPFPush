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

package org.onepf.opfpush.nokia;

/**
 * This class contains the {@code NokiaNotificationProvider} specific constants.
 *
 * @author Roman Savin
 * @since 17.12.14
 */
public final class NokiaPushConstants {

    /**
     * The name of the {@code NokiaNotificationProvider}
     */
    public static final String PROVIDER_NAME = "Nokia Push";

    static final String NOKIA_MANUFACTURER = "Nokia";
    static final String NOKIA_STORE_APP_PACKAGE = "com.nokia.store";

    static final String PERMISSION_RECEIVE = "com.nokia.pushnotifications.permission.RECEIVE";
    static final String PERMISSION_C2D_MESSAGE_SUFFIX = ".permission.C2D_MESSAGE";

    private NokiaPushConstants() {
        throw new UnsupportedOperationException();
    }
}
