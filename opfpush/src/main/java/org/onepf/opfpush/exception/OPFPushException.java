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

package org.onepf.opfpush.exception;

/**
 * Thrown when try to work with OPFPush in wrong way.
 *
 * @author Kirill Rozov
 * @since 05.09.14.
 */
public class OPFPushException extends RuntimeException {

    public OPFPushException(String detailMessage) {
        super(detailMessage);
    }

    public OPFPushException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }

    public OPFPushException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
