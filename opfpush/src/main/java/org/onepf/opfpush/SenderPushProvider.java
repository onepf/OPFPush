package org.onepf.opfpush;

import android.support.annotation.NonNull;

import org.onepf.opfpush.model.Message;

/**
 * PushProvider that can send messages to server.
 *
 * @author Kirill Rozov
 * @since 10/13/14.
 */
public interface SenderPushProvider extends PushProvider {

    /**
     * Send message to server. Method must be consumed async.
     *
     * @throws IllegalStateException If try send message when provider isn't registered.
     */
    void send(@NonNull Message msg);
}
