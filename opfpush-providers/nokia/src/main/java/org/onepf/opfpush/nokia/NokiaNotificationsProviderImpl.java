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

package org.onepf.opfpush.nokia;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nokia.push.PushRegistrar;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfutils.OPFChecks;
import org.onepf.opfutils.OPFLog;

import java.util.Locale;

import static org.onepf.opfpush.nokia.NokiaPushConstants.NOKIA_MANUFACTURER;
import static org.onepf.opfpush.nokia.NokiaPushConstants.NOKIA_STORE_APP_PACKAGE;
import static org.onepf.opfpush.nokia.NokiaPushConstants.PERMISSION_C2D_MESSAGE_SUFFIX;
import static org.onepf.opfpush.nokia.NokiaPushConstants.PERMISSION_RECEIVE;
import static org.onepf.opfpush.nokia.NokiaPushConstants.PROVIDER_NAME;

/**
 * Nokia Notification push provider implementation.
 *
 * @author Roman Savin
 * @since 23.03.2015
 */
class NokiaNotificationsProviderImpl extends BasePushProvider implements NokiaPushProvider {

    @NonNull
    private final String[] sendersIds;

    public NokiaNotificationsProviderImpl(@NonNull final Context context,
                                          @NonNull final String... sendersIds) {
        super(context, PROVIDER_NAME, NOKIA_STORE_APP_PACKAGE);
        this.sendersIds = sendersIds;
    }

    @Override
    public boolean isAvailable() {
        OPFLog.logMethod();
        if (Build.MANUFACTURER.equals(NOKIA_MANUFACTURER)) {
            try {
                PushRegistrar.checkDevice(getContext());
                return true;
            } catch (UnsupportedOperationException exception) {
                OPFLog.d(exception.toString());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void checkManifest() {
        OPFLog.logMethod();
        super.checkManifest();
        final Context context = getContext();
        PushRegistrar.checkManifest(context);

        OPFChecks.checkPermission(context, PERMISSION_RECEIVE);

        final String c2dmPermission = context.getPackageName() + PERMISSION_C2D_MESSAGE_SUFFIX;
        OPFChecks.checkPermission(context, c2dmPermission);

        OPFChecks.checkService(context, new ComponentName(context, NokiaNotificationService.class));
    }

    @Override
    public void onRegistrationInvalid() {
        //nothing
    }

    /**
     * Gets how long (in milliseconds) the {@link #isRegistered()} property is valid.
     *
     * @return Value set by {@link #setRegisteredOnServer(android.content.Context, boolean)}
     * or {@link PushRegistrar#DEFAULT_ON_SERVER_LIFESPAN_MS} if not set.
     */
    @Override
    public long getRegisterOnServerLifespan() {
        return PushRegistrar.getRegisterOnServerLifespan(getContext());
    }

    /**
     * Sets whether the device was successfully registered in the server side.
     */
    @Override
    public void setRegisteredOnServer(@NonNull final Context context, final boolean flag) {
        OPFLog.logMethod(context, flag);
        PushRegistrar.setRegisteredOnServer(context, flag);
    }

    /**
     * Sets how long (in milliseconds) the {@link #isRegistered()} flag is valid.
     */
    @Override
    public void setRegisterOnServerLifespan(@NonNull final Context context, final long lifespan) {
        OPFLog.logMethod(context, lifespan);
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
    @Override
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
        OPFLog.logMethod();
        return PushRegistrar.isRegistered(getContext());
    }

    @Override
    public void register() {
        super.register();
        OPFLog.logMethod();
        if (!isUnregistrationPerforming()) {
            OPFLog.i("Start register NokiaNotificationsProvider.");
            PushRegistrar.register(getContext(), sendersIds);
        }
    }

    @Override
    public void unregister() {
        super.unregister();
        OPFLog.logMethod();
        if (!isRegistrationPerforming()) {
            OPFLog.i("Start unregister NokiaNotificationsProvider.");
            final Context context = getContext();
            PushRegistrar.unregister(context);
            PushRegistrar.onDestroy(context);
        }
    }

    @Override
    public void onUnavailable() {
        OPFLog.logMethod();
        PushRegistrar.onDestroy(getContext());
    }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder senderIdsBuilder = new StringBuilder("[");
        for (String senderID : sendersIds) {
            senderIdsBuilder.append(senderID).append(", ");
        }
        senderIdsBuilder.append(']');
        return String.format(Locale.US, "%s (senderId: '%s')", PROVIDER_NAME, senderIdsBuilder.toString());
    }
}
