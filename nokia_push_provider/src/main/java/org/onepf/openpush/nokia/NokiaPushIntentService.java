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
import android.content.Intent;

import com.nokia.push.PushBaseIntentService;

import org.onepf.openpush.OpenPushProviderHelper;
import org.onepf.openpush.PushListener;

/**
 * @author Anastasia Karimova
 * @since 08.07.2014
 */
public class NokiaPushIntentService extends PushBaseIntentService {

    private PushListener pushListener;

    public NokiaPushIntentService() {
        super("Nokia Push Client"); //Passed name will use as Thread name;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pushListener = OpenPushProviderHelper.getInternalPushListener();
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        pushListener.onMessage(context, intent.getExtras(), NokiaPushProvider.NAME);
    }

    @Override
    protected void onError(Context context, String errorMessage) {
        pushListener.onError(context, errorMessage, NokiaPushProvider.NAME);
    }

    @Override
    protected void onRegistered(Context context, String registrationToken) {
        pushListener.onRegistered(context, registrationToken, NokiaPushProvider.NAME);
    }

    @Override
    protected void onUnregistered(Context context, String oldRegistrationToken) {
        pushListener.onUnregistered(context, oldRegistrationToken, NokiaPushProvider.NAME);
    }

}
