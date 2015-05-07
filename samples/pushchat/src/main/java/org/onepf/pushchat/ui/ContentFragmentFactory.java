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

package org.onepf.pushchat.ui;

import android.support.annotation.NonNull;
import org.onepf.pushchat.ui.fragment.content.*;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public final class ContentFragmentFactory {

    public static final int STATE_FRAGMENT_POSITION = 0;
    public static final int MESSAGES_FRAGMENT_POSITION = 1;
    public static final int CONTACTS_FRAGMENT_POSITION = 2;
    public static final int ABOUT_FRAGMENT_POSITION = 3;

    private ContentFragmentFactory() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static BaseContentFragment getFragmentByPosition(final int position) {
        switch (position) {
            case STATE_FRAGMENT_POSITION:
                return StateFragment.newInstance();
            case MESSAGES_FRAGMENT_POSITION:
                return MessagesFragment.newInstance();
            case CONTACTS_FRAGMENT_POSITION:
                return ContactsFragment.newInstance();
            case ABOUT_FRAGMENT_POSITION:
                return AboutFragment.newInstance();
            default:
                throw new IllegalArgumentException("Wrong position");
        }
    }
}
