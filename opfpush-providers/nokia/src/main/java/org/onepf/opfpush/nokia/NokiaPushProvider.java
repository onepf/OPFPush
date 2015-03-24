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

package org.onepf.opfpush.nokia;

import android.content.Context;
import android.support.annotation.NonNull;

import org.onepf.opfpush.pushprovider.PushProvider;

/**
 * @author Roman Savin
 * @since 23.03.2015
 */
public interface NokiaPushProvider extends PushProvider {

    long getRegisterOnServerLifespan();

    void setRegisteredOnServer(@NonNull final Context context, final boolean flag);

    void setRegisterOnServerLifespan(@NonNull final Context context, final long lifespan);

    boolean isRegisterOnServer();
}
