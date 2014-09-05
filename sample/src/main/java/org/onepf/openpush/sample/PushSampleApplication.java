/*******************************************************************************
 * Copyright 2014 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

package org.onepf.openpush.sample;

import android.app.Application;

import org.onepf.openpush.util.LogUtils;
import org.onepf.openpush.OpenPushProvider;
import org.onepf.openpush.Options;
import org.onepf.openpush.OpenPushStrictMode;
import org.onepf.openpush.gcm.GcmProvider;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class PushSampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initOpenPushLibrary();
    }

    private void initOpenPushLibrary() {
        LogUtils.enable();
        OpenPushStrictMode.enable();

        Options.Builder builder = new Options.Builder();
        builder.addProvider(new GcmProvider(this, "76325631570"));
        OpenPushProvider.getInstance().init(builder.build());
    }
}
