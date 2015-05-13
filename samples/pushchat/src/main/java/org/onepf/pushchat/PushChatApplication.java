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

package org.onepf.pushchat;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.adm.ADMProvider;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.gcm.GCMProvider;
import org.onepf.opfpush.nokia.NokiaNotificationsProvider;
import org.onepf.opfutils.OPFLog;
import org.onepf.pushchat.listener.PushEventListener;
import org.onepf.pushchat.utils.UuidGenerator;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class PushChatApplication extends Application {

    private static final String GCM_SENDER_ID = "707033278505";

    private static final String NOKIA_SENDER_ID = "pushsample";

    private String uuid;

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);

        OPFLog.setEnabled(BuildConfig.DEBUG, true);
        OPFLog.logMethod();

        uuid = UuidGenerator.generateUuid(this);

        OPFLog.i("Generated uuid : %s", uuid);
        final Configuration.Builder configBuilder = new Configuration.Builder()
                .addProviders(
                        new GCMProvider(this, GCM_SENDER_ID),
                        new ADMProvider(this),
                        new NokiaNotificationsProvider(this, NOKIA_SENDER_ID)
                )
                .setSelectSystemPreferred(true)
                .setEventListener(new PushEventListener());

        OPFPush.init(this, configBuilder.build());
        OPFPush.getHelper().register();
    }

    public String getUUID() {
        return uuid;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }
}
