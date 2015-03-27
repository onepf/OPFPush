package org.onepf.opfpush.pushsample.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import org.onepf.opfpush.OPFPush;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author antonpp
 * @since 27.03.15
 */
public final class Util {

    public static final long WIFI_WAIT_TIME = 200;
    public static final long MAX_WAIT_TIME_LONG_ATTEMPT = 128000;
    public static final long MAX_WAIT_TIME_ATTEMPT = 8000;

    public static final String KEY_UNREGISTERING_PROVIDER_PREFIX = "unregistering_provider_";


    private Util() {
        throw new UnsupportedOperationException();
    }

    public static String getProviderPreferenceKey(@NonNull final String prefix,
                                            @NonNull final String providerName) {
        return prefix + providerName.toLowerCase(Locale.US);
    }

    public static void setWifiEnabled(Activity activity, boolean enabled) {
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
        if (enabled) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            while (networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
                try {
                    Thread.sleep(WIFI_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void eraseHelperInstance() throws Exception {
        synchronized (OPFPush.class) {
            final Field instanceField = OPFPush.class.getDeclaredField("helper");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        }
    }

    public static final class Monitor {
        private final Object monitor = new Object();
        private boolean wasSignalled = false;

        public boolean doWait(long timeout) {
            synchronized (monitor) {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + timeout;
                try {
                    while (!wasSignalled) {
                        long waitTime = endTime - System.currentTimeMillis();
                        if (waitTime > 0) {
                            monitor.wait(waitTime);
                        } else {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            boolean result = wasSignalled;
            wasSignalled = false;
            return result;
        }

        public void doNotify() {
            synchronized (monitor) {
                wasSignalled = true;
                monitor.notify();
            }
        }
    }
}