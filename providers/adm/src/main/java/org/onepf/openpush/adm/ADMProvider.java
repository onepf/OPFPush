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
