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

package org.onepf.opfpush.unity;

import android.content.Context;
import android.support.annotation.NonNull;
import com.unity3d.player.UnityPlayer;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.configuration.Configuration;

public final class UnityHelper {

    private UnityHelper() {
        throw new UnsupportedOperationException();
    }

    public static void init(@NonNull final Context context, @NonNull final Configuration config) {
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                OPFPush.init(context, config);
            }
        });
    }

    public static void register() {
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                OPFPush.getHelper().register();
            }
        });
    }
}
