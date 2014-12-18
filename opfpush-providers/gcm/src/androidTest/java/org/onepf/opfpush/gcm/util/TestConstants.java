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

package org.onepf.opfpush.gcm.util;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 10/9/14.
 */
public final class TestConstants {

    private TestConstants() {
        throw new UnsupportedOperationException();
    }

    public static final String GOOGLE_PLAY_SERVICES_PACKAGE = "com.google.android.gms";

    public static final String SENDER_ID = "12191132";

    public static final String REGISTRATION_ID = "123abc";

    public static final String MESSAGE_EXTRA_KEY = "message";
}
