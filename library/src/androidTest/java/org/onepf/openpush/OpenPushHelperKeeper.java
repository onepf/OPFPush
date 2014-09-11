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

package org.onepf.openpush;

import android.content.Context;

import org.onepf.openpush.OpenPushHelper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Constructor;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
public final class OpenPushHelperKeeper {
    private static Constructor<OpenPushHelper> mOpenPushHelperConstructor;

    private static OpenPushHelper sOpenPushHelper;

    public static OpenPushHelper getInstance(Context context) {
        if (sOpenPushHelper == null) {
            createNewOpenPushHelper(context);
        }
        return sOpenPushHelper;
    }

    public static OpenPushHelper getNewInstance(Context context) {
        createNewOpenPushHelper(context);
        return sOpenPushHelper;
    }

    static void createNewOpenPushHelper(Context context) {
        try {
            if (mOpenPushHelperConstructor == null) {
                mOpenPushHelperConstructor =
                        OpenPushHelper.class.getDeclaredConstructor(Context.class);
                mOpenPushHelperConstructor.setAccessible(true);
            }
            sOpenPushHelper = mOpenPushHelperConstructor.newInstance(context);
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of OpenPushHelper.", e);
        }
    }

    private OpenPushHelperKeeper() {
    }
}
