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
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.PushProvider;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.model.RecoverablePushError;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import static org.onepf.opfpush.adm.ADMConstants.PROVIDER_NAME;
import static org.onepf.opfpush.model.RecoverablePushError.Type.SERVICE_NOT_AVAILABLE;
import static org.onepf.opfpush.model.UnrecoverablePushError.Type.AUTHENTICATION_FAILED;
import static org.onepf.opfpush.model.UnrecoverablePushError.Type.INVALID_SENDER;
import static org.onepf.opfpush.model.UnrecoverablePushError.Type.UNKNOWN_ERROR;

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

    @NonNull
    private final PreferencesProvider preferencesProvider;

    public ADMService() {
        super("ADMService");
        preferencesProvider = PreferencesProvider.getInstance(getApplicationContext());
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
        OPFLog.logMethod(OPFUtils.toString(intent));
        //ADM can receive messages even if it's unregistered. So we have to check ADM state.
        final PushProvider currentProvider = OPFPush.getHelper().getCurrentProvider();

        if (currentProvider != null
                && PROVIDER_NAME.equals(currentProvider.getName())
                && currentProvider.isRegistered()) {
            OPFLog.d("ADMProvider is registered");
            OPFPush.getHelper().getReceivedMessageHandler().onMessage(PROVIDER_NAME, intent.getExtras());
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
        OPFLog.logMethod(registrationId);
        preferencesProvider.saveRegistrationId(registrationId);
        preferencesProvider.removeAuthenticationFailedFlag();
        OPFPush.getHelper().getReceivedMessageHandler().onRegistered(PROVIDER_NAME, registrationId);
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
        OPFLog.logMethod(admRegistrationId);
        final String registrationId = admRegistrationId == null
                ? preferencesProvider.getRegistrationId()
                : admRegistrationId;
        preferencesProvider.reset();
        OPFPush.getHelper().getReceivedMessageHandler().onUnregistered(PROVIDER_NAME, registrationId);
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
        OPFLog.logMethod(errorId);
        final PushError error = convertError(errorId);
        OPFLog.d("ADM received error : " + error);

        final OPFPushHelper helper = OPFPush.getHelper();
        if (helper.isRegistering()) {
            //Registration Error
            preferencesProvider.removeAuthenticationFailedFlag();
        } else if (error.getType() == AUTHENTICATION_FAILED) {
            //Unregistration Error
            preferencesProvider.saveAuthenticationFailedFlag();
        }

        helper.getReceivedMessageHandler().onError(PROVIDER_NAME, error);
    }

    @NonNull
    private PushError convertError(@NonNull @ADMError final String errorId) {
        switch (errorId) {
            case ADMConstants.ERROR_SERVICE_NOT_AVAILABLE:
                return new RecoverablePushError(SERVICE_NOT_AVAILABLE, PROVIDER_NAME, errorId);
            case ADMConstants.ERROR_INVALID_SENDER:
                return new UnrecoverablePushError(INVALID_SENDER, PROVIDER_NAME, errorId);
            case ADMConstants.ERROR_AUTHENTICATION_FAILED:
                return new UnrecoverablePushError(AUTHENTICATION_FAILED, PROVIDER_NAME, errorId);
            default:
                return new UnrecoverablePushError(UNKNOWN_ERROR, PROVIDER_NAME, errorId);
        }
    }
}
