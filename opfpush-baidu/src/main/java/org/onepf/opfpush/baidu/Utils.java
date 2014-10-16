package org.onepf.opfpush.baidu;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
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

    public static Bundle messageToBundle(String message) throws JSONException {
        return toBundle(new JSONObject(message));
    }


    public static void toBundle(Bundle to, String key, JSONArray jsonArray) {
        Object o = toBundleInner(jsonArray);
        if (o instanceof Bundle[]) {
            to.putParcelableArray(key, (Bundle[]) o);
        } else if (o instanceof boolean[]) {
            to.putBooleanArray(key, (boolean[]) o);
        } else if (o instanceof int[]) {
            to.putIntArray(key, (int[]) o);
        } else if (o instanceof long[]) {
            to.putLongArray(key, (long[]) o);
        } else if (o instanceof double[]) {
            to.putDoubleArray(key, (double[]) o);
        } else if (o instanceof String[]) {
            to.putStringArray(key, (String[]) o);
        }
    }

    public static Object toBundleInner(JSONArray jsonArray) {
        int length = jsonArray.length();

        try {
            JSONObject firstObject = jsonArray.getJSONObject(0);
            Bundle[] values = new Bundle[length];
            values[0] = toBundle(firstObject);
            for (int i = 1; i < length; i++) {
                values[i] = toBundle(jsonArray.getJSONObject(i));
            }
            return values;
        } catch (JSONException ignored) {
        }

        try {
            return toBundleInner(jsonArray.getJSONArray(0));
        } catch (JSONException ignored) {
        }

        try {
            boolean firstBoolean = jsonArray.getBoolean(0);
            boolean[] values = new boolean[length];
            values[0] = firstBoolean;
            for (int i = 1; i < length; i++) {
                values[i] = jsonArray.getBoolean(i);
            }
            return values;
        } catch (JSONException ignored) {
        }

        try {
            int firstInt = jsonArray.getInt(0);
            int[] values = new int[length];
            values[0] = firstInt;
            for (int i = 1; i < length; i++) {
                values[i] = jsonArray.getInt(i);
            }
            return values;
        } catch (JSONException ignored) {
        }

        try {
            long firstLong = jsonArray.getLong(0);
            long[] values = new long[length];
            values[0] = firstLong;
            for (int i = 1; i < length; i++) {
                values[i] = jsonArray.getLong(i);
            }
            return values;
        } catch (JSONException ignored) {
        }

        try {
            double firstDouble = jsonArray.getDouble(0);
            double[] values = new double[length];
            values[0] = firstDouble;
            for (int i = 1; i < length; i++) {
                values[i] = jsonArray.getDouble(i);
            }
            return values;
        } catch (JSONException ignored) {
        }

        try {
            String firstString = jsonArray.getString(0);
            String[] values = new String[length];
            values[0] = firstString;
            for (int i = 1; i < length; i++) {
                values[i] = jsonArray.getString(i);
            }
            return values;
        } catch (JSONException ignored) {
        }

        return null;
    }

    public static Bundle toBundle(JSONObject json) {
        Bundle bundle = new Bundle(json.length());
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            try {
                bundle.putBundle(key, toBundle(json.getJSONObject(key)));
                continue;
            } catch (JSONException ignored) {
            }

            try {
                toBundle(bundle, key, json.getJSONArray(key));
                continue;
            } catch (JSONException ignored) {
            }

            try {
                bundle.putBoolean(key, json.getBoolean(key));
                continue;
            } catch (JSONException ignored) {
            }

            try {
                long value = json.getLong(key);
                if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
                    bundle.putInt(key, (int) value);
                } else {
                    bundle.putLong(key, value);
                }
                continue;
            } catch (JSONException ignored) {
            }

            try {
                bundle.putDouble(key, json.getDouble(key));
                continue;
            } catch (JSONException ignored) {
            }

            try {
                bundle.putString(key, json.getString(key));
                continue;
            } catch (JSONException ignored) {
            }
        }
        return bundle;
    }
}
