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

package org.onepf.opfpush.pushsample.model.event;

import android.support.annotation.NonNull;

import org.onepf.opfpush.model.UnrecoverablePushError;

import java.util.Map;

/**
 * @author Roman Savin
 * @since 02.02.2015
 */
public final class NoAvailableProviderEvent {

    @NonNull
    private final Map<String, UnrecoverablePushError> pushErrors;

    public NoAvailableProviderEvent(@NonNull final Map<String, UnrecoverablePushError> pushErrors) {
        this.pushErrors = pushErrors;
    }

    @NonNull
    public Map<String, UnrecoverablePushError> getPushErrors() {
        return pushErrors;
    }
}
