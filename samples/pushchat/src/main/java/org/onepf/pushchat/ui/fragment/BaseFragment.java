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

package org.onepf.pushchat.ui.fragment;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.squareup.leakcanary.RefWatcher;
import org.onepf.pushchat.PushChatApplication;
import org.onepf.pushchat.ui.activity.MainActivity;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class BaseFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        final RefWatcher refWatcher = getPushChatApplication().getRefWatcher();
        refWatcher.watch(this);
    }

    @Nullable
    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    protected void closeDrawer() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.closeDrawer();
        }
    }

    protected PushChatApplication getPushChatApplication() {
        return (PushChatApplication) getActivity().getApplication();
    }
}
