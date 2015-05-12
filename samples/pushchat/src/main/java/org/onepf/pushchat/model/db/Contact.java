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

package org.onepf.pushchat.model.db;

import android.support.annotation.NonNull;

/**
 * @author Roman Savin
 * @since 07.05.2015
 */
public final class Contact {

    @NonNull
    private final String name;

    @NonNull
    private final String uuid;

    public Contact(@NonNull final String name, @NonNull final String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }
}
