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

package org.onepf.pushchat.utils;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
public final class Constants {

    public static final String MESSAGE_EXTRA_KEY = "message";
    public static final String SENDER_EXTRA_KEY = "sender";

    public static final String REGISTERED_ACTION = "org.onepf.pushchat.REGISTERED_ACTION";
    public static final String UNREGISTERED_ACTION = "org.onepf.pushchat.UNREGISTERED_ACTION";

    public static final String PROVIDER_NAME_EXTRA_KEY = "PROVIDER_NAME_EXTRA_KEY";
    public static final String REGISTRATION_ID_EXTRA_KEY = "REGISTRATION_ID_EXTRA_KEY";

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private Constants() {
        throw new UnsupportedOperationException();
    }
}
