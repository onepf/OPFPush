package org.onepf.opfpush;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
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
