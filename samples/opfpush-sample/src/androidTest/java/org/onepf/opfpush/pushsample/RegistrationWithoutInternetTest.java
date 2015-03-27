package org.onepf.opfpush.pushsample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import junit.framework.Assert;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.adm.ADMConstants;
import org.onepf.opfpush.gcm.GCMConstants;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.nokia.NokiaPushConstants;
import org.onepf.opfpush.pushsample.activity.DemoActivity;
import org.onepf.opfpush.pushsample.mock.MessageHandlerAdapter;
import org.onepf.opfpush.pushsample.mock.MockReceivedMessageHandler;
import org.onepf.opfpush.pushsample.util.Util.Monitor;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.onepf.opfpush.model.RecoverablePushError.Type.SERVICE_NOT_AVAILABLE;
import static org.onepf.opfpush.pushsample.util.Util.KEY_UNREGISTERING_PROVIDER_PREFIX;
import static org.onepf.opfpush.pushsample.util.Util.MAX_WAIT_TIME_ATTEMPT;
import static org.onepf.opfpush.pushsample.util.Util.MAX_WAIT_TIME_LONG_ATTEMPT;
import static org.onepf.opfpush.pushsample.util.Util.getProviderPreferenceKey;
import static org.onepf.opfpush.pushsample.util.Util.setWifiEnabled;

/**
 * @author antonpp
 * @since 24.03.15
 */
@LargeTest
public class RegistrationWithoutInternetTest extends ActivityInstrumentationTestCase2<DemoActivity> {

    private static final int ATTEMPTS = 4;

    private final Monitor registrationMonitor = new Monitor();
    private final Monitor registerAttemptMonitor = new Monitor();
    private final Monitor unregisterationMonitor = new Monitor();

    public RegistrationWithoutInternetTest() {
        super(DemoActivity.class);
    }

    private DemoActivity activity;
    private OPFPushHelper helper;
    private int registrationAttempt = 0;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        helper = OPFPush.getHelper();
        MockReceivedMessageHandler.addListenerToHelper(helper, new MessageHandler());
    }


    public void testRegistrationWithoutWifi() {
        setWifiEnabled(activity, true);

        if (!helper.isRegistered()) {
            if (!registrationMonitor.doWait(MAX_WAIT_TIME_ATTEMPT)) {
                Assert.fail("Application could not register on start");
            }
        }

        if (activity.findViewById(R.id.unregister_button).isEnabled()
                && (activity.findViewById(R.id.unregister_button).getVisibility() == View.VISIBLE)) {
            onView(withId(R.id.unregister_button)).perform(click());
        }

        if (!isUnregistered(activity)) {
            if (!unregisterationMonitor.doWait(MAX_WAIT_TIME_ATTEMPT)) {
                Assert.fail("Application could not unregister on start");
            }
        }

        setWifiEnabled(activity, false);
        onView(withId(R.id.register_button)).perform(click());

        registrationAttempt = 0;
        while (registrationAttempt < ATTEMPTS) {
            if (!registerAttemptMonitor.doWait(MAX_WAIT_TIME_LONG_ATTEMPT)) {
                Assert.fail("Application does not send SERVICE_NOT_AVAILABLE errors. Received messages: " + registrationAttempt);
            }
        }

        setWifiEnabled(activity, true);
        if (!helper.isRegistered()) {
            if (!registrationMonitor.doWait(MAX_WAIT_TIME_LONG_ATTEMPT)) {
                Assert.fail("Application could not register after wifi was turned on");
            }
        }
    }

    private boolean isUnregistered(Activity activity) {
        final SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_MULTI_PROCESS);
        final String[] providers = {GCMConstants.PROVIDER_NAME, ADMConstants.PROVIDER_NAME, NokiaPushConstants.PROVIDER_NAME};
        for (String providerName : providers) {
            if (sharedPreferences.getBoolean(getProviderPreferenceKey(KEY_UNREGISTERING_PROVIDER_PREFIX, providerName), false)) {
                return false;
            }
        }
        return true;
    }



    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        setWifiEnabled(activity, true);
    }

    private final class MessageHandler extends MessageHandlerAdapter {

        @Override
        public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
            registrationMonitor.doNotify();
        }

        @Override
        public void onUnregistered(@NonNull String providerName, @Nullable String oldRegistrationId) {
            unregisterationMonitor.doNotify();
        }

        @Override
        public void onRegistrationError(@NonNull String providerName, @NonNull PushError error) {
            if (error.getType() == SERVICE_NOT_AVAILABLE) {
                ++registrationAttempt;
                registerAttemptMonitor.doNotify();
            }
        }
    }
}
