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

package org.onepf.opfpush.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.onepf.opfpush.util.Utils;

/**
 * Represent message for send from client to server that uses in Google Cloud Messaging.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 18.09.14.
 */
public class Message implements Parcelable {

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

    private final String id;
    private final Bundle data;
    private final long timeToLeave;

    /**
     * Create new message.
     *
     * @param id   Message's ID.
     * @param data Messages' data to send.
     */
    public Message(@NonNull final String id, @NonNull final Bundle data) {
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
    public Message(@NonNull final String id, @NonNull final Bundle data, final long timeToLeave) {
        if (timeToLeave < 0) {
            throw new IllegalArgumentException(String.format("timeToLeave='%d'."
                    + " Time to leave must be non negative value.", timeToLeave));
        }
        this.id = id;
        this.data = data;
        this.timeToLeave = timeToLeave;
    }

    private Message(final Parcel parcel) {
        id = parcel.readString();
        timeToLeave = parcel.readLong();
        data = parcel.readBundle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(timeToLeave);
        dest.writeBundle(data);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Bundle getData() {
        return data;
    }

    public long getTimeToLeave() {
        return timeToLeave;
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{"
                + "messageId='" + id + '\''
                + ", data='" + Utils.toString(data)
                + '\''
                + ", TTL='" + timeToLeave + '\''
                + '}';
    }
}
