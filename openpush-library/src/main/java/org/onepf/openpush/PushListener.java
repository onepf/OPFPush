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

package org.onepf.openpush;

import android.content.Context;
import android.os.Bundle;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public abstract class PushListener {

    /**
     * Method invoked on an incoming message from the provider.
     *
     * @param context      current context
     * @param extras       message
     * @param providerName name of the current provider
     */
    public void onMessage(Context context, Bundle extras, String providerName) {
    }

    /**
     * Method invoked on an error from the provider.
     *
     * @param context      current context
     * @param message      error message
     * @param providerName name of the current provider
     */
    public void onError(Context context, String message, String providerName) {
    }

    /**
     * Method invoked on the registration event
     *
     * @param context      current context
     * @param token        current token
     * @param providerName name of the current provider
     */
    public void onRegistered(Context context, String token, String providerName) {
    }

    /**
     * Method invoked on the unregistration event.
     *
     * @param context      current context
     * @param token        current token
     * @param providerName name of the current provider
     */
    public void onUnregistered(Context context, String token, String providerName) {
    }
}
