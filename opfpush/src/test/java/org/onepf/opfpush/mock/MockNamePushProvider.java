package org.onepf.opfpush.mock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.BasePushProvider;
import org.robolectric.Robolectric;

/**
 * Created by antonpp on 24.02.15.
 */
public class MockNamePushProvider extends BasePushProvider {

    public static final String DEFAULT_HOST_APP_PACKAGE = "org.onepf.store";
    public static final String DEFAULT_NAME = MockNamePushProvider.class.getName();

    public MockNamePushProvider(@NonNull String name) {
        this(name, DEFAULT_HOST_APP_PACKAGE);
    }

    public MockNamePushProvider(@NonNull String name,
                                @NonNull String hotAppPackage) {
        super(Robolectric.application, name, hotAppPackage);
    }

    @Override
    public void register() {
        //nothing
    }

    @Override
    public void unregister() {
        //nothing
    }

    @Override
    public void onRegistrationInvalid() {
        //nothing
    }

    @Override
    public void onUnavailable() {
        //nothing
    }

    @Override
    public boolean isRegistered() {
        return false;
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        return null;
    }
}
