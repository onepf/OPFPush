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
 * Notification model class. It has the same fields as the {@code notification} parameter of a GCM downstream message.
 * https://developers.google.com/cloud-messaging/server-ref#notification-payload-support
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

    /**
     * Returns the field that indicates notification title.
     *
     * @return The field that indicates notification title.
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * Returns the field that indicates notification icon.
     *
     * @return The field that indicates notification icon.
     */
    @NonNull
    public String getIcon() {
        return icon;
    }

    /**
     * Returns the field that indicates notification body text.
     *
     * @return The field that indicates notification body text.
     */
    @Nullable
    public String getBody() {
        return body;
    }

    /**
     * Returns the field that indicates sound to be played.
     *
     * @return The field that indicates sound to be played. Supports only {@code default} currently.
     */
    @Nullable
    public String getSound() {
        return sound;
    }

    /**
     * Indicates whether each notification message results in a new entry on the notification center on Android.
     * If not set, each request creates a new notification. If set, and a notification with the same tag is already being shown,
     * the new notification replaces the existing one in notification center.
     *
     * @return Notification tag.
     */
    @Nullable
    public String getTag() {
        return tag;
    }

    /**
     * Returns the field that indicates color of the icon, expressed in #rrggbb format.
     *
     * @return The field that indicates color of the icon, expressed in #rrggbb format.
     */
    @Nullable
    public String getColor() {
        return color;
    }

    /**
     * The action associated with a user click on the notification.
     *
     * @return The action associated with a user click on the notification.
     */
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

        /**
         * Indicates notification title.
         *
         * @param title Notification title. Required parameter.
         * @return Builder instance.
         */
        public Builder setTitle(@NonNull final String title) {
            this.title = title;
            return this;
        }

        /**
         * Indicates notification icon. Sets value to myicon for drawable resource myicon.png. Required parameter.
         *
         * @param icon Notification icon. Required parameter.
         * @return Builder instance.
         */
        public Builder setIcon(@NonNull final String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Indicates notification body text.
         *
         * @param body Notification body text. Optional parameter.
         * @return Builder instance.
         */
        public Builder setBody(@Nullable final String body) {
            this.body = body;
            return this;
        }

        /**
         * Indicates sound to be played. Supports only default currently.
         *
         * @param sound Notification sound. Optional parameter.
         * @return Builder instance.
         */
        public Builder setSound(@Nullable final String sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Indicates whether each notification message results in a new entry on the notification center on Android.
         * If not set, each request creates a new notification. If set, and a notification with the same tag is already
         * being shown, the new notification replaces the existing one in notification center.
         *
         * @param tag Notification tag. Optional parameter.
         * @return Builder instance.
         */
        public Builder setTag(@Nullable final String tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Indicates color of the icon, expressed in #rrggbb format.
         *
         * @param color Indicates color of the icon, expressed in #rrggbb format.
         * @return Builder instance.
         */
        public Builder setColor(@Nullable final String color) {
            this.color = color;
            return this;
        }

        /**
         * The action associated with a user click on the notification.
         *
         * @param clickAction The action associated with a user click on the notification.
         * @return Builder instance.
         */
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
