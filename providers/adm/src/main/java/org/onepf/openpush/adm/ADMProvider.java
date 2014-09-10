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

package org.onepf.openpush.adm;

import android.content.Context;

import com.amazon.device.messaging.ADM;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.BasePushProvider;
import org.onepf.openpush.OpenPushException;

/**
 * Created by krozov on 06.09.14.
 */
public abstract class ADMProvider extends BasePushProvider {

    public static final String NAME = "Amazon Device Messaging";

    private final ADM mAdm;

    public ADMProvider(@NotNull Context context) {
        super(context, "com.amazon.device.messaging.ADM");
        mAdm = new ADM(getContext());
    }

    @Override
    public void register() {
        if (mAdm.getRegistrationId() == null) {
            mAdm.startRegister();
        } else {
            throw new OpenPushException("Amazon Device Messaging already registered.");
        }
    }

    @Override
    public void unregister() {
        if (mAdm.getRegistrationId() != null) {
            mAdm.startUnregister();
        } else {
            throw new OpenPushException("Amazon Device Messaging must be registered.");
        }
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable() && mAdm.isSupported();
    }

    @Override
    public boolean isRegistered() {
        return mAdm.getRegistrationId() != null;
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return mAdm.getRegistrationId();
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public String getHostAppPackage() {
        return "com.amazon.venezia";
    }
}
