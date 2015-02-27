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

package org.onepf.opfpush.unity.receiver;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.unity3d.player.UnityPlayer;

import org.onepf.opfpush.OPFPushReceiver;
import org.onepf.opfpush.model.OPFError;
import org.onepf.opfpush.unity.model.MessageEvent;
import org.onepf.opfpush.unity.model.NoAvailableProviderEvent;
import org.onepf.opfpush.unity.model.RegisteredEvent;
import org.onepf.opfpush.unity.model.UnregisteredEvent;
import org.onepf.opfpush.unity.util.NotificationUtils;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.onepf.opfpush.unity.util.Constants.MESSAGE_EXTRA_KEY;
import static org.onepf.opfpush.unity.util.Constants.PAYLOAD_EXTRA_KEY;

/**
 * @author Roman Savin
 * @since 25.12.14
 */
public class UnityOPFPushReceiver extends OPFPushReceiver {

    private static final String EVENT_RECEIVER = "OPFPush";
    private static final String INIT_SUCCEEDED_CALLBACK = "OnInitSucceeded";
    private static final String INIT_FAILED_CALLBACK = "OnInitFailed";

    @Override
    public void onMessage(@NonNull final Context context,
                          @NonNull final String providerName,
                          @Nullable final Bundle extras) {
        OPFLog.methodD(UnityOPFPushReceiver.class, "onMessage",
                context, providerName, OPFUtils.toString(extras));
        if (extras == null) {
            return;
        }

        String message = null;
        if (extras.containsKey(MESSAGE_EXTRA_KEY)) {
            message = extras.getString(MESSAGE_EXTRA_KEY);
        } else if (extras.containsKey(PAYLOAD_EXTRA_KEY)) {
            message = extras.getString(PAYLOAD_EXTRA_KEY);
        }

        if (message != null) {
            try {
                NotificationUtils.showNotification(
                        context,
                        "Message",
                        message
                );
                EventBus.getDefault().postSticky(new MessageEvent(URLDecoder.decode(message, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                OPFLog.e(e.getCause().toString());
            }
        }
    }

    @Override
    public void onDeletedMessages(@NonNull final Context context,
                                  @NonNull final String providerName,
                                  final int messagesCount) {
        OPFLog.methodD(UnityOPFPushReceiver.class, "onDeletedMessages", providerName, messagesCount);
    }

    @Override
    public void onRegistered(@NonNull final Context context,
                             @NonNull final String providerName,
                             @NonNull final String registrationId) {
        OPFLog.methodD(UnityOPFPushReceiver.class, "onRegistered", providerName, registrationId);
        EventBus.getDefault().postSticky(new RegisteredEvent(registrationId));

        UnityPlayer.UnitySendMessage(EVENT_RECEIVER, INIT_SUCCEEDED_CALLBACK, registrationId);
    }

    @Override
    public void onUnregistered(@NonNull final Context context,
                               @NonNull final String providerName,
                               @Nullable final String oldRegistrationId) {
        OPFLog.methodD(UnityOPFPushReceiver.class, "onUnregistered", providerName, oldRegistrationId);
        EventBus.getDefault().postSticky(new UnregisteredEvent(oldRegistrationId));
    }

    @Override
    public void onNoAvailableProvider(@NonNull final Context context,
                                      @NonNull final Map<String, OPFError> registrationErrors) {
        OPFLog.methodD(UnityOPFPushReceiver.class, "onNoAvailableProvider", context, registrationErrors);
        EventBus.getDefault().postSticky(new NoAvailableProviderEvent(registrationErrors));
    }
}
