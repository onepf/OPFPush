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

package org.onepf.opfpush;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Listener for push events about messages.
 *
 * @author Kirill Rozov
 * @since 10/7/14.
 */
public interface MessageListener {

    /**
     * New message received.
     *
     * @param providerName Name of provider received event.
     * @param extras       Data associated with message.
     */
    void onMessageReceive(@NonNull String providerName, @Nullable Bundle extras);

    /**
     * Notification about deleted messages. Not all provider send this data or not send
     * count of deleted messages (in this case this value will be negative).
     *
     * @param providerName  Name of provider received event.
     * @param messagesCount Count of messages. Negative value if no info about count.
     */
    void onDeletedMessages(@NonNull String providerName, int messagesCount);
}
