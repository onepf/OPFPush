package org.onepf.openpush.gcm;

import android.os.Bundle;

/**
 * Created by  Kirill Rozov on 18.09.14.
 */
public class GCMMessage {
    private String mMessageId;
    private Bundle mData;
    private long mTimeToLeave;

    public GCMMessage(String messageId, Bundle data) {
       this(messageId, data, 0);
    }

    public GCMMessage(String messageId, Bundle data, long timeToLeave) {
        mMessageId = messageId;
        mData = data;
        mTimeToLeave = timeToLeave;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public Bundle getData() {
        return mData;
    }

    public long getTimeToLeave() {
        return mTimeToLeave;
    }
}
