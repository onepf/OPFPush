package org.onepf.openpush.sample;

import android.app.Application;

import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.OpenPushLog;
import org.onepf.openpush.Options;
import org.onepf.openpush.gcm.GCMProvider;

/**
 * @author Kirill Rozov
 * @since 29.09.14.
 */
public class PushSampleApplication extends Application {

    public static final String GCM_SENDER_ID = "76325631570";

    @Override
    public void onCreate() {
        super.onCreate();

        OpenPushLog.setLogEnable(true);
        OpenPushHelper openPushHelper = OpenPushHelper.getInstance(this);
        Options.Builder builder = new Options.Builder();
        builder.addProviders(new GCMProvider(this, GCM_SENDER_ID));
        openPushHelper.init(builder.build());
    }
}
