package org.onepf.opfpush.mock;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by krozov on 24.09.14.
 */
public class MockInfinityRegisterPushProvider extends MockPushProvider {
    public MockInfinityRegisterPushProvider(@NonNull Context context) {
        super(context);
    }

    public MockInfinityRegisterPushProvider(@NonNull Context context, String name) {
        super(context, name);
    }

    public MockInfinityRegisterPushProvider(@NonNull Context context, @NonNull String name, boolean available) {
        super(context, name, available);
    }

    public MockInfinityRegisterPushProvider(@NonNull Context context, @NonNull String name, @NonNull String hotAppPackage) {
        super(context, name, hotAppPackage);
    }

    public MockInfinityRegisterPushProvider(@NonNull Context context, @NonNull String name, boolean available, @NonNull String hotAppPackage) {
        super(context, name, available, hotAppPackage);
    }

    @Override
    public void register() {
    }
}
