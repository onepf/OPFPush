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

package org.onepf.opfpush.unity.listener;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.unity3d.player.UnityPlayer;
import org.onepf.opfpush.listener.EventListener;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfpush.unity.utils.UnityJsonGenerator;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author Roman Savin
 * @since 22.05.2015
 */
public class PushEventListener implements EventListener {

    private static final String EVENT_RECEIVER = "OPFPush";
    private static final String MESSAGE_CALLBACK = "OnMessage";
    private static final String DELETED_MESSAGES_CALLBACK = "OnDeletedMessages";
    private static final String REGISTERED_CALLBACK = "OnRegistered";
    private static final String UNREGISTERED_CALLBACK = "OnUnregistered";
    private static final String NO_AVAILABLE_PROVIDER_CALLBACK = "OnNoAvailableProvider";

    @Override
    public void onMessage(@NonNull final Context context,
                          @NonNull final String providerName,
                          @Nullable final Bundle extras) {
        OPFLog.logMethod(context, providerName, OPFUtils.toString(extras));
        try {
            UnityPlayer.UnitySendMessage(EVENT_RECEIVER, MESSAGE_CALLBACK,
                    UnityJsonGenerator.getOnMessageJson(providerName, extras));
        } catch (IOException e) {
            OPFLog.e(e.getMessage());
        }
    }

    @Override
    public void onDeletedMessages(@NonNull final Context context,
                                  @NonNull final String providerName,
                                  final int messagesCount) {
        OPFLog.logMethod(providerName, messagesCount);
        try {
            UnityPlayer.UnitySendMessage(EVENT_RECEIVER, DELETED_MESSAGES_CALLBACK,
                    UnityJsonGenerator.getOnDeletedJson(providerName, messagesCount));
        } catch (IOException e) {
            OPFLog.e(e.getMessage());
        }
    }

    @Override
    public void onRegistered(@NonNull final Context context,
                             @NonNull final String providerName,
                             @NonNull final String registrationId) {
        OPFLog.logMethod(providerName, registrationId);
        try {
            UnityPlayer.UnitySendMessage(EVENT_RECEIVER, REGISTERED_CALLBACK,
                    UnityJsonGenerator.getOnRegisteredJson(providerName, registrationId));
        } catch (IOException e) {
            OPFLog.e(e.getMessage());
        }
    }

    @Override
    public void onUnregistered(@NonNull final Context context,
                               @NonNull final String providerName,
                               @Nullable final String oldRegistrationId) {
        OPFLog.logMethod(providerName, oldRegistrationId);
        try {
            UnityPlayer.UnitySendMessage(EVENT_RECEIVER, UNREGISTERED_CALLBACK,
                    UnityJsonGenerator.getOnUnregisteredJson(providerName, oldRegistrationId));
        } catch (IOException e) {
            OPFLog.e(e.getMessage());
        }
    }

    @Override
    public void onNoAvailableProvider(@NonNull final Context context,
                                      @NonNull final Map<String, UnrecoverablePushError> pushErrors) {
        OPFLog.logMethod(context, pushErrors);
        try {
            UnityPlayer.UnitySendMessage(EVENT_RECEIVER, NO_AVAILABLE_PROVIDER_CALLBACK,
                    UnityJsonGenerator.getOnNoAvailableProviderJson(pushErrors));
        } catch (IOException e) {
            OPFLog.e(e.getMessage());
        }
    }
}
