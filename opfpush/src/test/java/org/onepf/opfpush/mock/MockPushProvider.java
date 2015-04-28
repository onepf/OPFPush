package org.onepf.opfpush.mock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.listener.CheckManifestHandler;
import org.onepf.opfpush.model.AvailabilityResult;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.onepf.opfpush.testutil.Util;

/**
 * @author antonpp
 * @since 14.04.15
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class MockPushProvider implements PushProvider {

    private final AvailabilityResult availabilityResult;
    private final String name;
    private final String hostAppPackage;
    private final boolean useOnlyOnError;
    private PushError regError;
    private PushError unregError;
    private String regId;

    private MockPushProvider(AvailabilityResult availabilityResult, @NonNull String name,
                             @NonNull String hostAppPackage, PushError regError, PushError unregError, boolean useOnlyOnError) {
        this.availabilityResult = availabilityResult;
        this.name = name;
        this.regError = regError;
        this.unregError = unregError;
        this.useOnlyOnError = useOnlyOnError;
        this.hostAppPackage = hostAppPackage;
    }

    public void setUnregError(PushError unregError) {
        this.unregError = unregError;
    }

    public void setRegError(PushError regError) {
        this.regError = regError;
    }

    @Override
    public void register() {
        if (regError == null) {
            regId = Util.getRandomStrings(1, Util.RANDOM_STRING_LENGTH)[0];
            OPFPush.getHelper().getReceivedMessageHandler().onRegistered(name, regId);
        } else {
            if (useOnlyOnError) {
                OPFPush.getHelper().getReceivedMessageHandler().onError(name, regError);
            } else {
                OPFPush.getHelper().getReceivedMessageHandler().onRegistrationError(name, regError);
            }
        }
    }

    @Override
    public void unregister() {
        if (unregError == null) {
            final String oldRegId = regId;
            regId = null;
            OPFPush.getHelper().getReceivedMessageHandler().onUnregistered(name, oldRegId);
        } else {
            if (useOnlyOnError) {
                OPFPush.getHelper().getReceivedMessageHandler().onError(name, unregError);
            } else {
                OPFPush.getHelper().getReceivedMessageHandler().onUnregistrationError(name, unregError);
            }
        }
    }

    @NonNull
    @Override
    public AvailabilityResult getAvailabilityResult() {
        return availabilityResult;
    }

    @Override
    public boolean isRegistered() {
        return regId != null;
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        return regId;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getHostAppPackage() {
        return hostAppPackage;
    }

    @Override
    public void checkManifest(@Nullable CheckManifestHandler checkManifestHandler) {
        // nothing
    }

    @Override
    public void onRegistrationInvalid() {
        regId = null;
    }

    @Override
    public void onUnavailable() {
        regId = null;
    }

    public static class Builder {

        private static final String DEFAULT_HOST_APP_PACKAGE = "org.onepf.store";

        private AvailabilityResult availabilityResult;
        private String name;
        private String hostAppPackage = DEFAULT_HOST_APP_PACKAGE;
        private PushError regError;
        private PushError unregError;
        private boolean useOnlyOnError;

        public Builder setName(final String name) {
            this.name = name;
            return this;
        }

        public void setHostAppPackage(String hostAppPackage) {
            this.hostAppPackage = hostAppPackage;
        }

        public Builder setRegError(final PushError regError) {
            this.regError = regError;
            return this;
        }

        public Builder setUnregError(final PushError unregError) {
            this.unregError = unregError;
            return this;
        }

        public Builder setUseOnlyOnError(final boolean useOnlyOnError) {
            this.useOnlyOnError = useOnlyOnError;
            return this;
        }

        public Builder setAvailabilityResult(AvailabilityResult availabilityResult) {
            this.availabilityResult = availabilityResult;
            return this;
        }

        @SuppressWarnings("PMD.AccessorClassGeneration")
        public MockPushProvider build() {
            if (name == null) {
                throw new IllegalArgumentException("Name must be set");
            }
            return new MockPushProvider(availabilityResult, name, hostAppPackage, regError, unregError, useOnlyOnError);
        }
    }
}
