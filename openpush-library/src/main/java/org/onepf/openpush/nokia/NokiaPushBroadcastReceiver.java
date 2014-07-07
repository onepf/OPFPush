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

package org.onepf.openpush.nokia;

import android.content.Context;

import com.nokia.push.PushBroadcastReceiver;

/**
 * @author Anastasia Karimova
 * @since 08.07.2014
 */
public final class NokiaPushBroadcastReceiver extends PushBroadcastReceiver {

    @Override
    protected String getPushIntentServiceClassName(Context context) {
//        return super.getPushIntentServiceClassName(context);
        return NokiaPushIntentService.class.getCanonicalName();
    }
}
