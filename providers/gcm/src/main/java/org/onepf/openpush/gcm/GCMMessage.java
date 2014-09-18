package org.onepf.openpush.gcm;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

/**
 * Created by  Kirill Rozov on 18.09.14.
 */
public class GCMMessage {
    private String mId;
    private Bundle mData;
    private long mTimeToLeave;

    public GCMMessage(String id, Bundle data) {
       this(id, data, 0);
    }

    public GCMMessage(String id, Bundle data, long timeToLeave) {
        mId = id;
        mData = data;
        mTimeToLeave = timeToLeave;
    }

    @NotNull
    public String getId() {
        return mId;
    }

    @NotNull
    public Bundle getData() {
        return mData;
    }

    public long getTimeToLeave() {
        return mTimeToLeave;
    }

    @NotNull
    @Override
    public String toString() {
        return "GCMMessage{" +
                "messageId='" + mId + '\'' +
                ", data=" + mData +
                ", TTL=" + mTimeToLeave +
                '}';
    }
}
