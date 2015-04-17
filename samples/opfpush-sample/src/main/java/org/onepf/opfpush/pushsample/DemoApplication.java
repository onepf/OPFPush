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

package org.onepf.opfpush.pushsample;

import android.app.Application;

import org.onepf.opfutils.OPFLog;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.adm.ADMProvider;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.gcm.GCMProvider;
import org.onepf.opfpush.nokia.NokiaNotificationsProvider;
import org.onepf.opfpush.pushsample.listener.DemoEventListener;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class DemoApplication extends Application {

    private static final String GCM_SENDER_ID = "707033278505";

    private static final String NOKIA_SENDER_ID = "pushsample";

    @Override
    public void onCreate() {
        super.onCreate();
        OPFLog.setEnabled(BuildConfig.DEBUG, true);
        OPFLog.logMethod();

        final Configuration.Builder configBuilder = new Configuration.Builder()
                .addProviders(
                        new GCMProvider(this, GCM_SENDER_ID),
                        new ADMProvider(this),
                        new NokiaNotificationsProvider(this, NOKIA_SENDER_ID)
                )
                .setSelectSystemPreferred(true)
                .setEventListener(new DemoEventListener(this));

        OPFPush.init(this, configBuilder.build());
        OPFPush.getHelper().register();
    }
}
