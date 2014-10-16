package org.onepf.opfpush.baidu;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Kirill Rozov
 * @since 10/16/14.
 */
public final class Utils {

    public static final String TAG_DIVIDER = ",";

    public static List<String> getTagsList(String originalText) {
        if (TextUtils.isEmpty(originalText)) {
            return null;
        }

        return originalText.contains(TAG_DIVIDER) ?
                Arrays.asList(originalText) : Arrays.asList(originalText.split(TAG_DIVIDER));
    }

    // Get ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        if (context == null || metaKey == null) {
            return null;
        }

        String apiKey = null;
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle metaData = null;
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        }
        return apiKey;
    }

    private Utils() {
    }
}
