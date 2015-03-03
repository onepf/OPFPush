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

package org.onepf.opfpush.model;

import android.support.annotation.Nullable;

/**
 * Indicates state of {@link org.onepf.opfpush.OPFPushHelper}.
 * Is used internally by library. Don't use this directly in an application.
 *
 * @author Roman Savin
 * @since 08.12.14
 */
public enum State {

    /**
     * Indicates that unregistration has been completed.
     */
    UNREGISTERED(0),

    /**
     * Indicates that registration has been completed.
     */
    REGISTERED(2),

    /**
     * Indicates that registration is being performed.
     */
    REGISTERING(3);

    private int value;

    State(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Nullable
    public static State fromValue(final int fromValue) {
        final State[] values = values();
        for (final State state : values) {
            final int stateValue = state.getValue();
            if (stateValue == fromValue) {
                return state;
            }
        }

        return null;
    }
}
