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

package org.onepf.opfpush.adm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.messaging.ADMConstants;
import com.amazon.device.messaging.ADMMessageHandlerBase;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.PushProvider;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.adm.Constants.NAME;

/**
 * This class allows your app to receive messages sent via ADM.
 * <p/>
 * All methods on this class are called on a background thread with a wake lock held.
 * It is safe to do long-running operations in these methods.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 06.09.14.
 */
public class ADMService extends ADMMessageHandlerBase {

    public ADMService() {
        super("ADMService");
    }

    /**
     * Called each time ADM delivers a message to an instance of your app.
     *
     * @param intent An intent containing the message and associated data.
     *               You extract the message content from the set of extras attached
     *               to the {@code com.amazon.device.messaging.intent.RECEIVE intent}.
     *               For an example of defining the behavior of the onMessage() callback,
     *               see SampleADMMessageHandler.java in the ADMMessenger sample app.
     */
    @Override
    protected void onMessage(@NonNull final Intent intent) {
        OPFPushLog.methodD(ADMService.class, "onMessage", OPFUtils.toString(intent));
        //ADM can receive messages even if it's unregistered. So we have to check ADM state.
        final PushProvider currentProvider = OPFPush.getHelper().getCurrentProvider();

        if (currentProvider != null
                && NAME.equals(currentProvider.getName())
                && currentProvider.isRegistered()) {
            OPFPushLog.d("ADMProvider is registered");
            OPFPush.getHelper().getReceivedMessageHandler().onMessage(NAME, intent.getExtras());
        }
    }

    /**
     * Called when a registration request succeeds.
     * ADM may call this message in response to your app calling startRegister()
     * or if ADM has updated the registration ID for this app instance.
     *
     * @param registrationId The new registration ID for the instance of your app.
     *                       Pass this value to your components that are using ADM to send messages.
     *                       The {@link com.amazon.device.messaging.ADM#getRegistrationId()}
     *                       method also obtains the registration ID for an instance of your app.
     */
    @Override
    protected void onRegistered(@NonNull final String registrationId) {
        OPFPushLog.methodD(ADMService.class, "onRegistered", "registrationId");
        RegIdStorage.getInstance(getApplicationContext()).saveRegistrationId(registrationId);
        OPFPush.getHelper().getReceivedMessageHandler().onRegistered(NAME, registrationId);
    }

    /**
     * Called on successful unregistration. This method may be called in response to your app
     * calling startUnregister() or if ADM has unregistered the app for some reason (typically
     * because the device has lost its association with a user's Amazon account).
     * If this message is called, your app should notify your components that are using ADM
     * to send messages, so that they know this instance of your app is no longer a valid recipient.
     *
     * @param admRegistrationId The registration ID for the instance of your app that is now unregistered.
     *                          This ID is no longer a valid destination for messages.
     *                          Calling {@link com.amazon.device.messaging.ADM#getRegistrationId()}
     *                          will show the registration ID for an unregistered app as {@code null}.
     */
    @Override
    protected void onUnregistered(@Nullable final String admRegistrationId) {
        OPFPushLog.methodD(ADMService.class, "onUnregistered", "admRegistrationId");
        final RegIdStorage settings = RegIdStorage.getInstance(getApplicationContext());
        final String registrationId = admRegistrationId == null
                ? settings.getRegistrationId()
                : admRegistrationId;
        settings.reset();
        OPFPush.getHelper().getReceivedMessageHandler().onUnregistered(NAME, registrationId);
    }

    /**
     * Called when a registration request fails.
     * You should consider a registration error fatal.
     * In response, your app may degrade gracefully,
     * or you may wish to notify the user that this part of your app's functionality is not available.
     *
     * @param errorId One of the following values:
     *                {@link ADMConstants#ERROR_AUTHENTICATION_FAILED},
     *                {@link ADMConstants#ERROR_INVALID_SENDER},
     *                {@link ADMConstants#ERROR_SERVICE_NOT_AVAILABLE}.
     */
    @Override
    protected void onRegistrationError(@NonNull @ADMError final String errorId) {
        OPFPushLog.methodD(ADMService.class, "onRegistrationError", errorId);
        final OPFError error = convertError(errorId);
        OPFPushLog.d("Converted error : " + error);

        OPFPush.getHelper().getReceivedMessageHandler().onError(NAME, error);
    }

    @NonNull
    private OPFError convertError(@NonNull @ADMError final String errorId) {
        final OPFError error;
        switch (errorId) {
            case ADMConstants.ERROR_SERVICE_NOT_AVAILABLE:
                error = OPFError.SERVICE_NOT_AVAILABLE;
                break;
            case ADMConstants.ERROR_INVALID_SENDER:
                error = OPFError.INVALID_SENDER;
                break;
            case ADMConstants.ERROR_AUTHENTICATION_FAILED:
                error = OPFError.AUTHENTICATION_FAILED;
                break;
            default:
                throw new OPFPushException(String.format("Unknown error '%s'.", errorId));
        }
        return error;
    }
}
