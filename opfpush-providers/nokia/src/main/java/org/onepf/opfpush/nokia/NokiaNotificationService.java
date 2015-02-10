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

package org.onepf.opfpush.nokia;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.nokia.push.PushBaseIntentService;
import com.nokia.push.PushConstants;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.exception.OPFPushException;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfutils.OPFLog;

import static org.onepf.opfpush.nokia.NokiaPushConstants.PROVIDER_NAME;

/**
 * Service for handling communication from Nokia Push Notifications service.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 05.09.2014
 */
public class NokiaNotificationService extends PushBaseIntentService {

    public NokiaNotificationService() {
        super("Nokia Push Client"); //Passed name will use as Thread name;
    }

    /**
     * Called after a device has been registered.
     *
     * @param appContext     Application's context.
     * @param registrationId The registration id returned by the Push Notifications service.
     */
    @Override
    protected void onRegistered(@NonNull final Context appContext,
                                @NonNull final String registrationId) {
        OPFLog.methodD(NokiaNotificationService.class, "onRegistered",
                appContext, registrationId);
        OPFPush.getHelper().getReceivedMessageHandler().onRegistered(PROVIDER_NAME, registrationId);
    }

    /**
     * Called after a device has been unregistered.
     *
     * @param appContext        Application's context.
     * @param oldRegistrationId the registration id that was previously registered.
     */
    @Override
    protected void onUnregistered(@NonNull final Context appContext,
                                  @NonNull final String oldRegistrationId) {
        OPFLog.methodD(NokiaNotificationService.class, "onUnregistered",
                appContext, oldRegistrationId);
        OPFPush.getHelper().getReceivedMessageHandler().onUnregistered(PROVIDER_NAME, oldRegistrationId);
    }

    /**
     * Called when a Push Notification message has been received.
     *
     * @param appContext Application's context.
     * @param intent     Intent containing the message payload as extras.
     */
    @Override
    protected void onMessage(@NonNull final Context appContext, @NonNull final Intent intent) {
        OPFLog.methodD(NokiaNotificationService.class, "onMessage", appContext, intent);
        OPFPush.getHelper().getReceivedMessageHandler().onMessage(PROVIDER_NAME, intent.getExtras());
    }

    /**
     * Called when the Push Notifications server tells pending messages
     * have been deleted because the device was idle.
     *
     * @param appContext Application's context.
     * @param total      Total number of collapsed messages.
     */
    @Override
    protected void onDeletedMessages(@NonNull final Context appContext, final int total) {
        OPFLog.methodD(NokiaNotificationService.class, "onDeletedMessages", appContext, total);
        OPFPush.getHelper().getReceivedMessageHandler().onDeletedMessages(PROVIDER_NAME, total);
    }

    /**
     * Called on registration or unregistration error.
     *
     * @param appContext Application's context.
     * @param errorId    Error id returned by the Push Notifications service.
     */
    @Override
    protected void onError(@NonNull final Context appContext,
                           @NonNull @NokiaNotificationsError final String errorId) {
        OPFLog.methodD(NokiaNotificationService.class, "onError", appContext, errorId);
        OPFPush.getHelper().getReceivedMessageHandler().onError(PROVIDER_NAME, convertError(errorId));
    }

    /**
     * Called on a registration error that could be retried.
     * By default, it does nothing and returns true,
     * but could be overridden to change that behavior and/or display the error.
     *
     * @param appContext Application's context.
     * @param errorId    Error id returned by the Push Notifications service.
     * @return If true, failed operation will be retried (using exponential backoff).
     */
    @Override
    protected boolean onRecoverableError(@NonNull final Context appContext,
                                         @NonNull @NokiaNotificationsError final String errorId) {
        OPFLog.methodD(NokiaNotificationService.class, "onRecoverableError",
                appContext, errorId);
        OPFPush.getHelper().getReceivedMessageHandler()
                .onRegistrationError(PROVIDER_NAME, convertError(errorId));
        return false;
    }

    @NonNull
    private static OPFError convertError(@NonNull @NokiaNotificationsError String errorId) {
        switch (errorId) {
            case PushConstants.ERROR_INVALID_PARAMETERS:
                return OPFError.INVALID_PARAMETERS;
            case PushConstants.ERROR_SERVICE_NOT_AVAILABLE:
                return OPFError.SERVICE_NOT_AVAILABLE;
            case PushConstants.ERROR_INVALID_SENDER:
                return OPFError.INVALID_SENDER;
            default:
                throw new OPFPushException(String.format("Unknown error '%s'.", errorId));
        }
    }
}
