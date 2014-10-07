package org.onepf.opfpush.gcm;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Represent message for send from client to server that uses in Google Cloud Messaging.
 *
 * @author Kirill Rozov
 * @since 18.09.14.
 */
public class GCMMessage {
    private String mId;
    private Bundle mData;
    private long mTimeToLeave;

    /**
     * Create new message.
     *
     * @param id   Message's ID.
     * @param data Messages' data to send.
     */
    public GCMMessage(@NonNull String id, @NonNull Bundle data) {
        this(id, data, 0);
    }

    /**
     * Create new message.
     *
     * @param id          Message's ID.
     * @param data        Messages' data to send.
     * @param timeToLeave How long message is valid. Set 0 to default value.
     */
    public GCMMessage(@NonNull String id, @NonNull Bundle data, long timeToLeave) {
        if (timeToLeave < 0) {
            throw new IllegalArgumentException(String.format("timeToLeave='%d'." +
                    " Time to leave must be non negative value.", timeToLeave));
        }
        mId = id;
        mData = data;
        mTimeToLeave = timeToLeave;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public Bundle getData() {
        return mData;
    }

    public long getTimeToLeave() {
        return mTimeToLeave;
    }

    @NonNull
    @Override
    public String toString() {
        return "GCMMessage{" +
                "messageId='" + mId + '\'' +
                ", data=" + mData +
                ", TTL=" + mTimeToLeave +
                '}';
    }
}
