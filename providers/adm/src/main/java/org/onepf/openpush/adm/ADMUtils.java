package org.onepf.openpush.adm;

import android.os.Build;

/**
 * Created by  Kirill Rozov on 18.09.14.
 */
public final class ADMUtils {
    private static final String AMAZON_MANUFACTURER = "Amazon";
    private static final String KINDLE_FIRE_MODEL = "Kindle Fire";

    /**
     * Check is current device is one of Kindle Fire.
     *
     * @return Is Kindle Fire device.
     */
    public static boolean isKindleFire() {
        return Build.MANUFACTURER.equals(AMAZON_MANUFACTURER)
                && (Build.MODEL.equals(KINDLE_FIRE_MODEL) || Build.MODEL.startsWith("KF"));
    }

    private ADMUtils() {
    }
}
