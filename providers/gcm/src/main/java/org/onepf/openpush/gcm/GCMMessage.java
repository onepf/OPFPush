package org.onepf.openpush.gcm;

import android.os.Bundle;
import android.support.annotation.NonNull;

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
