package org.onepf.openpush.mock;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by krozov on 24.09.14.
 */
public class MockInfinityUnregisterPushProvider extends MockPushProvider {
    public MockInfinityUnregisterPushProvider(@NonNull Context context) {
        super(context);
    }

    public MockInfinityUnregisterPushProvider(@NonNull Context context, String name) {
        super(context, name);
    }

    public MockInfinityUnregisterPushProvider(@NonNull Context context, @NonNull String name, boolean available) {
        super(context, name, available);
    }

    public MockInfinityUnregisterPushProvider(@NonNull Context context, @NonNull String name, @NonNull String hotAppPackage) {
        super(context, name, hotAppPackage);
    }

    public MockInfinityUnregisterPushProvider(@NonNull Context context, @NonNull String name, boolean available, @NonNull String hotAppPackage) {
        super(context, name, available, hotAppPackage);
    }

    @Override
    public void unregister() {
    }
}
