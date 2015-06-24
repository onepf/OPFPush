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

package org.onepf.opfpush.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * TODO: javadoc
 *
 * @author Roman Savin
 * @since 23.06.2015
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class NotificationPayload {

    @NonNull
    private final String title;

    @NonNull
    private final String icon;

    @Nullable
    private final String body;

    @Nullable
    private final String sound;

    @Nullable
    private final String tag;

    @Nullable
    private final String color;

    @Nullable
    private final String clickAction;

    private NotificationPayload(@NonNull final String title,
                                @NonNull final String icon,
                                @Nullable final String body,
                                @Nullable final String sound,
                                @Nullable final String tag,
                                @Nullable final String color,
                                @Nullable final String clickAction) {
        this.title = title;
        this.icon = icon;
        this.body = body;
        this.sound = sound;
        this.tag = tag;
        this.color = color;
        this.clickAction = clickAction;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getIcon() {
        return icon;
    }

    @Nullable
    public String getBody() {
        return body;
    }

    @Nullable
    public String getSound() {
        return sound;
    }

    @Nullable
    public String getTag() {
        return tag;
    }

    @Nullable
    public String getColor() {
        return color;
    }

    @Nullable
    public String getClickAction() {
        return clickAction;
    }

    @Override
    public String toString() {
        return "NotificationPayload : {"
                + " title : " + title
                + ", icon : " + icon
                + ", body : " + body
                + ", sound : " + sound
                + ", tag : " + tag
                + ", color : " + color
                + ", clickAction : " + clickAction
                + "}";
    }

    public static final class Builder {

        @Nullable
        private String title;

        @Nullable
        private String icon;

        @Nullable
        private String body;

        @Nullable
        private String sound;

        @Nullable
        private String tag;

        @Nullable
        private String color;

        @Nullable
        private String clickAction;

        public Builder setTitle(@NonNull final String title) {
            this.title = title;
            return this;
        }

        public Builder setIcon(@NonNull final String icon) {
            this.icon = icon;
            return this;
        }

        public Builder setBody(@Nullable final String body) {
            this.body = body;
            return this;
        }

        public Builder setSound(@Nullable final String sound) {
            this.sound = sound;
            return this;
        }

        public Builder setTag(@Nullable final String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setColor(@Nullable final String color) {
            this.color = color;
            return this;
        }

        public Builder setClickAction(@Nullable final String clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        @SuppressWarnings("PMD.AccessorClassGeneration")
        public NotificationPayload build() {
            if (TextUtils.isEmpty(title)) {
                throw new IllegalArgumentException("Title can't be empty");
            }

            if (TextUtils.isEmpty(icon)) {
                throw new IllegalArgumentException("Icon can't be empty");
            }

            return new NotificationPayload(title, icon, body, sound, tag, color, clickAction);
        }
    }
}
