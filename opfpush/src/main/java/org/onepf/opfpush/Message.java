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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represent message for send from client to server that uses in Google Cloud Messaging.
 *
 * @author Kirill Rozov
 * @since 18.09.14.
 */
public class Message implements Parcelable {
    private final String mId;
    private final Bundle mData;
    private final long mTimeToLeave;

    /**
     * Create new message.
     *
     * @param id   Message's ID.
     * @param data Messages' data to send.
     */
    public Message(@NonNull String id, @NonNull Bundle data) {
        this(id, data, 0);
    }

    /**
     * Create new message.
     *
     * @param id          Message's ID.
     * @param data        Messages' data to send.
     * @param timeToLeave How long message is valid. Set 0 to default value.
     * @throws java.lang.IllegalArgumentException If {@code timeToLeave} is negative.
     */
    public Message(@NonNull String id, @NonNull Bundle data, long timeToLeave) {
        if (timeToLeave < 0) {
            throw new IllegalArgumentException(String.format("timeToLeave='%d'." +
                    " Time to leave must be non negative value.", timeToLeave));
        }
        mId = id;
        mData = data;
        mTimeToLeave = timeToLeave;
    }

    private Message(Parcel parcel) {
        mId = parcel.readString();
        mTimeToLeave = parcel.readLong();
        mData = parcel.readBundle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeLong(mTimeToLeave);
        dest.writeBundle(mData);
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
        return "Message{" +
                "messageId='" + mId + '\'' +
                ", data='" + mData + '\'' +
                ", TTL='" + mTimeToLeave + '\'' +
                '}';
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
