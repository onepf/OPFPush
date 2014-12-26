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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nokia.push.PushRegistrar;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfpush.OPFPushLog;

import static org.onepf.opfpush.nokia.Constants.NAME;
import static org.onepf.opfpush.nokia.Constants.NOKIA_MANUFACTURER;
import static org.onepf.opfpush.nokia.Constants.NOKIA_STORE_APP_PACKAGE;

/**
 * Nokia Notification push provider implementation.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @see <a href="http://developer.nokia.com/resources/library/nokia-x/nokia-notifications.html">Nokia Notification</a>
 * @since 06.09.14
 */
public class NokiaNotificationsProvider extends BasePushProvider {

    @NonNull
    private final String[] sendersIds;

    public NokiaNotificationsProvider(@NonNull final Context context,
                                      @NonNull final String... sendersID) {
        super(context, NAME, NOKIA_STORE_APP_PACKAGE);
        sendersIds = sendersID;
    }

    @Override
    public boolean isAvailable() {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "isAvailable");
        if (Build.MANUFACTURER.equals(NOKIA_MANUFACTURER)) {
            try {
                PushRegistrar.checkDevice(getContext());
                return true;
            } catch (UnsupportedOperationException exception) {
                OPFPushLog.e(exception.getCause().toString());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean checkManifest() {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "checkManifest");
        try {
            PushRegistrar.checkManifest(getContext());
            return super.checkManifest();
        } catch (UnsupportedOperationException exception) {
            OPFPushLog.e(exception.getCause().toString());
            return false;
        }
    }

    /**
     * Gets how long (in milliseconds) the {@link #isRegistered()} property is valid.
     *
     * @return Value set by {@link #setRegisteredOnServer(android.content.Context, boolean)}
     * or {@link PushRegistrar#DEFAULT_ON_SERVER_LIFESPAN_MS} if not set.
     */
    public long getRegisterOnServerLifespan() {
        return PushRegistrar.getRegisterOnServerLifespan(getContext());
    }

    /**
     * Sets whether the device was successfully registered in the server side.
     */
    public void setRegisteredOnServer(@NonNull final Context context, final boolean flag) {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "setRegisteredOnServer", context, flag);
        PushRegistrar.setRegisteredOnServer(context, flag);
    }

    /**
     * Sets how long (in milliseconds) the {@link #isRegistered()} flag is valid.
     */
    public void setRegisterOnServerLifespan(@NonNull final Context context, final long lifespan) {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "setRegisterOnServerLifespan",
                context, lifespan);
        PushRegistrar.setRegisterOnServerLifespan(context, lifespan);
    }

    /**
     * Checks whether the device was successfully registered in the server side,
     * as set by {@link #setRegisteredOnServer(android.content.Context, boolean)}.
     * To avoid the scenario where the device sends the registration to the server
     * but the server loses it, this flag has an expiration date,
     * which is DEFAULT_ON_SERVER_LIFESPAN_MS by default (but can be changed
     * by {@link #setRegisterOnServerLifespan(android.content.Context, long)}).
     */
    public boolean isRegisterOnServer() {
        return PushRegistrar.isRegisteredOnServer(getContext());
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        return PushRegistrar.getRegistrationId(getContext());
    }

    @Override
    public boolean isRegistered() {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "isRegistered");
        return PushRegistrar.isRegistered(getContext());
    }

    @Override
    public void register() {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "register");
        PushRegistrar.register(getContext(), sendersIds);
    }

    @Override
    public void unregister() {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "unregister");
        PushRegistrar.unregister(getContext());
        PushRegistrar.onDestroy(getContext());
    }

    @Override
    public void onUnavailable() {
        OPFPushLog.methodD(NokiaNotificationsProvider.class, "onUnavailable");
        PushRegistrar.onDestroy(getContext());
    }
}
