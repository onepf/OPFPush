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

package org.onepf.opfpush.adm;

import com.amazon.device.messaging.ADMMessageReceiver;

/**
 * Forwards the Amazon Device Messaging (ADM) messages to {@link ADMService}.
 * The ADM client on the device uses broadcast intents to dispatch messages to your app.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 06.09.14.
 */
public final class ADMReceiver extends ADMMessageReceiver {

    public ADMReceiver() {
        super(ADMService.class);
    }
}
