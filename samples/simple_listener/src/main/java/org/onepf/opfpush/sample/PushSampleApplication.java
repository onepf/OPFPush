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

package org.onepf.opfpush.sample;

import android.app.Application;

import org.onepf.opfpush.ExponentialBackoff;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.Options;
import org.onepf.opfpush.gcm.GCMProvider;

/**
 * @author Kirill Rozov
 * @since 29.09.14.
 */
public class PushSampleApplication extends Application {

    public static final String GCM_SENDER_ID = "76325631570";

    @Override
    public void onCreate() {
        super.onCreate();

        OPFPushLog.setLogEnable(true);

        Options.Builder builder = new Options.Builder();
        builder.addProviders(new GCMProvider(this, GCM_SENDER_ID))
                .setRecoverProvider(true)
                .setSelectSystemPreferred(true)
                .setBackoff(new ExponentialBackoff(Integer.MAX_VALUE));
        Options options = builder.build();

        OPFPushHelper.getInstance(this).init(options);
    }
}
