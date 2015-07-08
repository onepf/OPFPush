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
import org.onepf.pushchat.ui.fragment.content.AboutFragment;
import org.onepf.pushchat.ui.fragment.content.BaseContentFragment;
import org.onepf.pushchat.ui.fragment.content.ContactsFragment;
import org.onepf.pushchat.ui.fragment.content.MessagesFragment;
import org.onepf.pushchat.ui.fragment.content.StateFragment;
import org.onepf.pushchat.ui.fragment.content.TopicsFragment;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public final class ContentFragmentFactory {

    private ContentFragmentFactory() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static BaseContentFragment getFragmentByPosition(final int position) {
        switch (position) {
            case StateFragment.POSITION:
                return StateFragment.newInstance();
            case TopicsFragment.POSITION:
                return TopicsFragment.newInstance();
            case MessagesFragment.POSITION:
                return MessagesFragment.newInstance();
            case ContactsFragment.POSITION:
                return ContactsFragment.newInstance();
            case AboutFragment.POSITION:
                return AboutFragment.newInstance();
            default:
                throw new IllegalArgumentException("Wrong position");
        }
    }
}
