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

package org.onepf.opfpush.unity;

import android.content.Context;
import com.unity3d.player.UnityPlayer;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.unity.listener.UnityEventListener;

public class UnityHelper {

    public static void init(final Context context, final Configuration config) {
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                OPFPush.init(context, config);
                OPFPush.getHelper().register();
            }
        });
    }
}
