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

package org.onepf.pushchat.model.response;

import android.support.annotation.NonNull;

/**
 * @author Roman Savin
 * @since 19.04.2015
 */
public final class RegistrationResponse {

    @NonNull
    public final String uuid;

    @NonNull
    public final String providerName;

    @NonNull
    public final String registrationId;

    public RegistrationResponse(@NonNull final String uuid,
                                @NonNull final String providerName,
                                @NonNull final String registrationId) {
        this.uuid = uuid;
        this.providerName = providerName;
        this.registrationId = registrationId;
    }
}
