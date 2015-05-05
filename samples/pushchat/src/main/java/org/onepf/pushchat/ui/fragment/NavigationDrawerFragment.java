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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.onepf.pushchat.R;
import org.onepf.pushchat.ui.ContentFragmentFactory;
import org.onepf.pushchat.ui.fragment.content.BaseContentFragment;
import org.onepf.pushchat.utils.FragmentUtils;
import org.onepf.pushchat.utils.StateController;

import static org.onepf.pushchat.model.PushState.REGISTERED;

public class NavigationDrawerFragment extends BaseFragment {

    private static final int STATE_POSITION = 0;
    private static final int MESSAGES_POSITION = 1;

    private static final String STATE_SELECTED_POSITION_KEY = "STATE_SELECTED_POSITION_KEY";

    private ListView drawerListView;

    private int currentSelectedPosition = 0;

    private String[] titles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titles = new String[]{
                getString(R.string.title_state_fragment),
                getString(R.string.title_messages_fragment),
                getString(R.string.title_contacts_fragment),
                getString(R.string.title_about_fragment)
        };

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION_KEY);
        } else {
            currentSelectedPosition =
                    StateController.getState(getActivity()) == REGISTERED ?
                            MESSAGES_POSITION :
                            STATE_POSITION;
        }

        selectItem(currentSelectedPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        drawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        drawerListView.setAdapter(new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                titles));
        drawerListView.setItemChecked(currentSelectedPosition, true);
        return drawerListView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        drawerListView = null;
    }

    private void selectItem(int position) {
        currentSelectedPosition = position;
        if (drawerListView != null) {
            drawerListView.setItemChecked(position, true);
        }
        setToolbarTitle(titles[position]);
        closeDrawer();

        final BaseContentFragment contentFragment = ContentFragmentFactory.getFragmentByPosition(position);
        FragmentUtils.replace(getFragmentManager(), contentFragment, contentFragment.getClass().getName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION_KEY, currentSelectedPosition);
    }
}
