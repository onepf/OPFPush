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

package org.onepf.pushchat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.onepf.pushchat.PushChatApplication;
import org.onepf.pushchat.R;
import org.onepf.pushchat.controller.StateController;
import org.onepf.pushchat.db.DatabaseHelper;
import org.onepf.pushchat.ui.fragment.NavigationDrawerFragment;
import org.onepf.pushchat.ui.fragment.content.BaseContentFragment;
import org.onepf.pushchat.ui.fragment.content.MessagesFragment;
import org.onepf.pushchat.ui.fragment.content.StateFragment;
import org.onepf.pushchat.utils.FragmentUtils;

import static android.content.Intent.ACTION_SEND;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.onepf.pushchat.model.PushState.REGISTERED;
import static org.onepf.pushchat.ui.activity.MainActivity.MainActivityReceiver.*;


public class MainActivity extends ActionBarActivity {

    public static final String OPEN_MESSAGES_FRAGMENT_ACTION = "OPEN_MESSAGES_FRAGMENT_ACTION";

    private static final String IS_SHARE_MENU_ITEM_VISIBLE_KEY = "IS_SHARE_MENU_ITEM_VISIBLE_KEY";

    private static final String IS_CLEAR_MENU_ITEM_VISIBLE_KEY = "IS_CLEAR_MENU_ITEM_VISIBLE_KEY";

    private NavigationDrawerFragment navigationDrawerFragment = NavigationDrawerFragment.newInstance();

    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;

    private ProgressBar progressBar;

    private MenuItem shareMenuItem;
    private MenuItem clearMenuItem;

    private boolean isShareMenuItemVisible;

    private boolean isClearMenuItemVisible;

    private MainActivityReceiver receiver;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        setupNavigationDrawer();
        if (savedInstanceState == null) {
            isShareMenuItemVisible = false;

            FragmentUtils.add(
                    getSupportFragmentManager(),
                    navigationDrawerFragment,
                    R.id.navaigation_drawer_container,
                    NavigationDrawerFragment.class.getName()
            );

            final BaseContentFragment fragment;
            if (StateController.getState(this) == REGISTERED) {
                fragment = MessagesFragment.newInstance();
            } else {
                fragment = StateFragment.newInstance();
            }

            FragmentUtils.add(
                    getSupportFragmentManager(),
                    fragment,
                    fragment.getClass().getName()
            );
        } else {
            isShareMenuItemVisible = savedInstanceState.getBoolean(IS_SHARE_MENU_ITEM_VISIBLE_KEY, false);
            isClearMenuItemVisible = savedInstanceState.getBoolean(IS_CLEAR_MENU_ITEM_VISIBLE_KEY, false);
        }
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (OPEN_MESSAGES_FRAGMENT_ACTION.equals(intent.getAction()) && navigationDrawerFragment != null) {
            navigationDrawerFragment.selectItem(MessagesFragment.POSITION);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_SHARE_MENU_ITEM_VISIBLE_KEY, isShareMenuItemVisible);
        outState.putBoolean(IS_CLEAR_MENU_ITEM_VISIBLE_KEY, isClearMenuItemVisible);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int itemId = item.getItemId();
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (itemId == R.id.action_share) {
            onShareClick();
            return true;
        } else if (itemId == R.id.action_clear) {
            onClearClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        shareMenuItem = menu.findItem(R.id.action_share);
        clearMenuItem = menu.findItem(R.id.action_clear);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull final Menu menu) {
        shareMenuItem.setVisible(isShareMenuItemVisible);
        clearMenuItem.setVisible(isClearMenuItemVisible);

        return true;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    public void showProgressbar() {
        if (progressBar != null) {
            progressBar.setVisibility(VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(GONE);
        }
    }

    public void showShareButton() {
        isShareMenuItemVisible = true;
        invalidateOptionsMenu();
    }

    public void hideShareButton() {
        isShareMenuItemVisible = false;
        invalidateOptionsMenu();
    }

    public void showClearButton() {
        isClearMenuItemVisible = true;
        invalidateOptionsMenu();
    }

    public void hideClearButton() {
        isClearMenuItemVisible = false;
        invalidateOptionsMenu();
    }

    public void setNavigationDrawerCheckedItem(final int position) {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.setItemChecked(position);
        }
    }

    private PushChatApplication getPushChatApplication() {
        return (PushChatApplication) getApplication();
    }

    private void registerReceiver() {
        if (receiver == null) {
            receiver = new MainActivityReceiver();
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SHOW_PROGRESS_BAR_ACTION);
            intentFilter.addAction(HIDE_PROGRESS_BAR_ACTION);
            intentFilter.addAction(SHOW_GCM_ERROR_DIALOG_ACTION);
            registerReceiver(receiver, intentFilter);
        }
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void setupNavigationDrawer() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void showGcmErrorDialog(final int errorCode) {
        if (errorCode != -1) {
            GooglePlayServicesUtil.showErrorDialogFragment(errorCode, this, 0);
        }
    }

    private void onShareClick() {
        final Intent intent = new Intent(ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.uuid_fmt, getPushChatApplication().getUUID()));
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getString(R.string.share_intent_chooser_title)));
    }

    private void onClearClick() {
        DatabaseHelper.getInstance(this).deleteMessages();
    }

    public class MainActivityReceiver extends BroadcastReceiver {

        public static final String SHOW_PROGRESS_BAR_ACTION = "SHOW_PROGRESS_BAR_ACTION";
        public static final String HIDE_PROGRESS_BAR_ACTION = "HIDE_PROGRESS_BAR_ACTION";
        public static final String SHOW_GCM_ERROR_DIALOG_ACTION = "SHOW_GCM_ERROR_DIALOG_ACTION";

        public static final String GCM_ERROR_CODE_EXTRA_KEY = "GCM_ERROR_CODE_EXTRA_KEY";

        @Override
        public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case SHOW_PROGRESS_BAR_ACTION:
                    showProgressbar();
                    break;
                case HIDE_PROGRESS_BAR_ACTION:
                    hideProgressBar();
                    break;
                case SHOW_GCM_ERROR_DIALOG_ACTION:
                    showGcmErrorDialog(intent.getIntExtra(GCM_ERROR_CODE_EXTRA_KEY, -1));
                    break;
            }
        }
    }
}
