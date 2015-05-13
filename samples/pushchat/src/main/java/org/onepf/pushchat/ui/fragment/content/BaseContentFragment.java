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

package org.onepf.pushchat.ui.fragment.content;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import org.onepf.pushchat.ui.activity.MainActivity;
import org.onepf.pushchat.ui.fragment.BaseFragment;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public abstract class BaseContentFragment extends BaseFragment {

    public abstract int getTitleResId();

    public abstract int getPosition();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setNavigationDrawerCheckedItem();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        setActionBarTitle();
    }

    protected void showProgressBar() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.showProgressbar();
        }
    }

    protected void hideProgressBar() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.hideProgressBar();
        }
    }

    protected void showShareButton() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.showShareButton();
        }
    }

    protected void hideShareButton() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.hideShareButton();
        }
    }

    protected void showClearButton() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.showClearButton();
        }
    }

    protected void hideClearButton() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.hideClearButton();
        }
    }

    protected void hideKeyboard(@Nullable final View view) {
        if (view != null) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void setActionBarTitle() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle(getString(getTitleResId()));
        }
    }

    protected void setNavigationDrawerCheckedItem() {
        final MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.setNavigationDrawerCheckedItem(getPosition());
        }
    }
}
