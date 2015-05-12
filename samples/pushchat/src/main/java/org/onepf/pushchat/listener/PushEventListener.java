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

package org.onepf.pushchat.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.GooglePlayServicesUtil;

import org.onepf.opfpush.gcm.GCMConstants;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;
import org.onepf.pushchat.R;
import org.onepf.pushchat.db.DatabaseHelper;
import org.onepf.pushchat.model.db.Message;
import org.onepf.pushchat.retrofit.NetworkController;
import org.onepf.pushchat.controller.NotificationController;
import org.onepf.pushchat.controller.StateController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static org.onepf.opfpush.model.UnrecoverablePushError.Type.AVAILABILITY_ERROR;
import static org.onepf.pushchat.ui.activity.MainActivity.MainActivityReceiver.GCM_ERROR_CODE_EXTRA_KEY;
import static org.onepf.pushchat.ui.activity.MainActivity.MainActivityReceiver.SHOW_GCM_ERROR_DIALOG_ACTION;
import static org.onepf.pushchat.utils.Constants.MESSAGE_EXTRA_KEY;
import static org.onepf.pushchat.utils.Constants.SENDER_EXTRA_KEY;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class PushEventListener implements EventListener {

    @Override
    public void onMessage(@NonNull final Context context,
                          @NonNull final String providerName,
                          @Nullable final Bundle extras) {
        OPFLog.logMethod(providerName, OPFUtils.toString(extras));
        if (extras == null) {
            return;
        }

        final String message = extras.getString(MESSAGE_EXTRA_KEY);
        final String senderUuid = extras.getString(SENDER_EXTRA_KEY);

        if (message != null && senderUuid != null) {
            String decodedMessage;
            try {
                decodedMessage = URLDecoder.decode(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                OPFLog.e(e.getMessage());
                decodedMessage = message;
            }

            NotificationController.getInstance().showNotification(
                    context,
                    context.getString(R.string.message_notification_title),
                    decodedMessage
            );

            DatabaseHelper.getInstance(context).addMessage(new Message(
                            senderUuid,
                            decodedMessage,
                            System.currentTimeMillis())
            );
        }
    }

    @Override
    public void onDeletedMessages(@NonNull final Context context,
                                  @NonNull final String providerName,
                                  final int messagesCount) {
        OPFLog.logMethod(providerName, messagesCount);
    }

    @Override
    public void onRegistered(@NonNull final Context context,
                             @NonNull final String providerName,
                             @NonNull final String registrationId) {
        OPFLog.logMethod(providerName, registrationId);

        StateController.putNoAvailableProviderValue(context, false);
        NetworkController.getInstance().register(context, providerName, registrationId);
    }

    @Override
    public void onUnregistered(@NonNull final Context context,
                               @NonNull final String providerName,
                               @Nullable final String registrationId) {
        OPFLog.logMethod(providerName, registrationId);
        NetworkController.getInstance().unregister(context);
    }

    @Override
    public void onNoAvailableProvider(@NonNull final Context context,
                                      @NonNull final Map<String, UnrecoverablePushError> pushErrors) {
        OPFLog.logMethod(context, pushErrors);

        StateController.putNoAvailableProviderValue(context, true);
        NetworkController.getInstance().unregister(context);

        //Log errors.
        if (!pushErrors.isEmpty()) {
            for (Map.Entry<String, UnrecoverablePushError> pushErrorEntry : pushErrors.entrySet()) {
                OPFLog.d("Push provider %1$ss is unavailable. Error : %2$s",
                        pushErrorEntry.getKey(), pushErrorEntry.getValue());
            }
        }

        if (pushErrors.containsKey(GCMConstants.PROVIDER_NAME)) {
            final UnrecoverablePushError gcmError = pushErrors.get(GCMConstants.PROVIDER_NAME);
            if (gcmError.getType() == AVAILABILITY_ERROR
                    && gcmError.getAvailabilityErrorCode() != null) {
                final int errorCode = gcmError.getAvailabilityErrorCode();
                if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
                    final Intent intent = new Intent(SHOW_GCM_ERROR_DIALOG_ACTION);
                    intent.putExtra(GCM_ERROR_CODE_EXTRA_KEY, errorCode);
                    context.sendBroadcast(intent);
                }
            }
        }
    }
}
