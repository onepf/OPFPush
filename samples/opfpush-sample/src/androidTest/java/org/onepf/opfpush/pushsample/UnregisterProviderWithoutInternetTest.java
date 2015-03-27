package org.onepf.opfpush.pushsample;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import junit.framework.Assert;

import org.onepf.opfpush.OPFPush;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.model.PushError;
import org.onepf.opfpush.pushsample.activity.DemoActivity;
import org.onepf.opfpush.pushsample.mock.MessageHandlerAdapter;
import org.onepf.opfpush.pushsample.mock.MockReceivedMessageHandler;
import org.onepf.opfpush.pushsample.util.Util.Monitor;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.onepf.opfpush.model.RecoverablePushError.Type.SERVICE_NOT_AVAILABLE;
import static org.onepf.opfpush.pushsample.util.Util.MAX_WAIT_TIME_ATTEMPT;
import static org.onepf.opfpush.pushsample.util.Util.MAX_WAIT_TIME_LONG_ATTEMPT;
import static org.onepf.opfpush.pushsample.util.Util.setWifiEnabled;

/**
 * @author antonpp
 * @since 27.03.15
 */
@LargeTest
public class UnregisterProviderWithoutInternetTest  extends ActivityInstrumentationTestCase2<DemoActivity> {
    public UnregisterProviderWithoutInternetTest() {
        super(DemoActivity.class);
    }

    private DemoActivity activity;
    private OPFPushHelper helper;


    private final Monitor registrationMonitor = new Monitor();
    private final Monitor unregisterationMonitor = new Monitor();
    private final Monitor unregisterationErrorMonitor = new Monitor();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        helper = OPFPush.getHelper();
        MockReceivedMessageHandler.addListenerToHelper(helper, new MessageHandler());
    }

    public void testUnregistrationWithoutWifi() {

        setWifiEnabled(activity, true);

        if (!helper.isRegistered()) {
            if (!registrationMonitor.doWait(MAX_WAIT_TIME_ATTEMPT)) {
                Assert.fail("Application could not register on start");
            }
        }

        final String regId = helper.getRegistrationId();

        setWifiEnabled(activity, false);

        onView(withId(R.id.unregister_button)).perform(click());

        onView(withId(R.id.info_text)).check(matches(withText(activity.getString(R.string.unregistered_state_fmt, regId))));

        if (!unregisterationErrorMonitor.doWait(MAX_WAIT_TIME_ATTEMPT)) {
            Assert.fail("Application did not send a message about failed unregistration");
        }

        setWifiEnabled(activity, true);

        if (!registrationMonitor.doWait(MAX_WAIT_TIME_LONG_ATTEMPT)) {
            Assert.fail("Application did not complete unregistration after wifi is turned on");
        }

    }

    private final class MessageHandler extends MessageHandlerAdapter {

        @Override
        public void onUnregistrationError(@NonNull String providerName, @NonNull PushError error) {
            if (error.getType() == SERVICE_NOT_AVAILABLE) {
                unregisterationErrorMonitor.doNotify();
            }
        }

        @Override
        public void onUnregistered(@NonNull String providerName, @Nullable String oldRegistrationId) {
            unregisterationMonitor.doNotify();
        }

        @Override
        public void onRegistered(@NonNull String providerName, @NonNull String registrationId) {
            registrationMonitor.doNotify();
        }
    }
}
