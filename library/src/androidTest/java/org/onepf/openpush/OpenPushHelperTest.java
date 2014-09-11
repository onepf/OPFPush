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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Constructor;

/**
 * Created by  Kirill Rozov on 11.09.14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OpenPushHelperTest {

    private Constructor<OpenPushHelper> mOpenPushHelperConstructor;

    OpenPushHelper createNewOpenPushHelper() {
        try {
            if (mOpenPushHelperConstructor == null) {
                mOpenPushHelperConstructor =
                        OpenPushHelper.class.getDeclaredConstructor(Context.class);
                mOpenPushHelperConstructor.setAccessible(true);
            }
            return mOpenPushHelperConstructor.newInstance(Robolectric.application);
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of OpenPushHelper.", e);
        }
    }

    @Test
    public void testInit() {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new StubPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = createNewOpenPushHelper();
        Assert.assertEquals(false, openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
        Assert.assertEquals(true, openPushHelper.isInitDone());
    }

    @Test(expected = OpenPushException.class)
    public void testInitTwice() {
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new StubPushProvider(Robolectric.application));
        OpenPushHelper openPushHelper = createNewOpenPushHelper();
        Assert.assertEquals(false, openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
        Assert.assertEquals(true, openPushHelper.isInitDone());
        openPushHelper.init(builder.build());
    }
}
