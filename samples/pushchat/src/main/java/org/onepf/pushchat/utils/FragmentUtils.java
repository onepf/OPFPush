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

package org.onepf.pushchat.utils;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.onepf.pushchat.R;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
@SuppressWarnings("unused")
public final class FragmentUtils {

    private FragmentUtils() {
        throw new UnsupportedOperationException();
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment) {
        replace(fragmentManager, fragment, R.id.fragment_container, null, false);
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment,
                           @IdRes final int fragmentContainerId) {
        replace(fragmentManager, fragment, fragmentContainerId, null, false);
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment,
                           @NonNull final String tag) {
        replace(fragmentManager, fragment, R.id.fragment_container, tag, false);
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment,
                           final boolean addToBackStack) {
        replace(fragmentManager, fragment, R.id.fragment_container, null, addToBackStack);
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment,
                           @IdRes final int fragmentContainerId,
                           @NonNull final String tag) {
        replace(fragmentManager, fragment, fragmentContainerId, tag, false);
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment,
                           @IdRes final int fragmentContainerId,
                           final boolean addToBackStack) {
        replace(fragmentManager, fragment, fragmentContainerId, null, addToBackStack);
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment,
                           @Nullable final String tag,
                           final boolean addToBackStack) {
        replace(fragmentManager, fragment, R.id.fragment_container, tag, addToBackStack);
    }

    public static void add(@NonNull final FragmentManager fragmentManager,
                           @NonNull final Fragment fragment,
                           @IdRes final int fragmentContainerId,
                           @Nullable final String tag,
                           final boolean addToBackStack) {
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragmentContainerId, fragment, tag);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }


    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment) {
        replace(fragmentManager, fragment, R.id.fragment_container, null, false);
    }

    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment,
                               @IdRes final int fragmentContainerId) {
        replace(fragmentManager, fragment, fragmentContainerId, null, false);
    }

    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment,
                               @NonNull final String tag) {
        replace(fragmentManager, fragment, R.id.fragment_container, tag, false);
    }

    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment,
                               final boolean addToBackStack) {
        replace(fragmentManager, fragment, R.id.fragment_container, null, addToBackStack);
    }

    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment,
                               @IdRes final int fragmentContainerId,
                               @NonNull final String tag) {
        replace(fragmentManager, fragment, fragmentContainerId, tag, false);
    }

    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment,
                               @IdRes final int fragmentContainerId,
                               final boolean addToBackStack) {
        replace(fragmentManager, fragment, fragmentContainerId, null, addToBackStack);
    }

    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment,
                               @Nullable final String tag,
                               final boolean addToBackStack) {
        replace(fragmentManager, fragment, R.id.fragment_container, tag, addToBackStack);
    }

    public static void replace(@NonNull final FragmentManager fragmentManager,
                               @NonNull final Fragment fragment,
                               @IdRes final int fragmentContainerId,
                               @Nullable final String tag,
                               final boolean addToBackStack) {
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(fragmentContainerId, fragment, tag);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }
}
