import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.baidu.Utils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Kirill Rozov
 * @since 10/16/14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class UtilsTest {

    @Test
    public void testMessageToBundle() throws Exception {
        final String titleKey = "Title";
        final String countKey = "Count";
        final String messageKey = "Message";
        final String arrayKey = "Array";
        final String innerObjKey = "obj";
        final String timeMillisKey = "TimeMillis";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(titleKey, "TITLE");
        jsonObject.put(messageKey, "MESSAGE");
        jsonObject.put(countKey, 10);
        jsonObject.put(timeMillisKey, (long) Integer.MAX_VALUE + 100L);

        JSONArray jsonInnerArray = new JSONArray();
        jsonInnerArray.put("First");
        jsonInnerArray.put("Second");
        jsonInnerArray.put("Third");
        jsonObject.put(arrayKey, jsonInnerArray);

        JSONObject innerJsonObject = new JSONObject();
        innerJsonObject.put(titleKey, "innerObj");
        jsonObject.put(innerObjKey, innerJsonObject);

        String message = jsonObject.toString();

        Bundle bundle = Utils.messageToBundle(message);
        assertEquals(jsonObject.length(), bundle.size());

        assertTrue(bundle.containsKey(titleKey));
        assertTrue(bundle.containsKey(messageKey));
        assertTrue(bundle.containsKey(countKey));
        assertTrue(bundle.containsKey(timeMillisKey));
        assertTrue(bundle.containsKey(arrayKey));
        assertTrue(bundle.containsKey(innerObjKey));

        assertEquals(jsonObject.getString(titleKey), bundle.getString(titleKey));
        assertEquals(jsonObject.getString(messageKey), bundle.getString(messageKey));
        assertEquals(jsonObject.getInt(countKey), bundle.getInt(countKey));
        assertEquals(jsonObject.getLong(timeMillisKey), bundle.getLong(timeMillisKey));

        String[] bundledArray = bundle.getStringArray(arrayKey);
        assertNotNull(bundledArray);
        assertArrayEquals(toStringArray(jsonInnerArray), bundledArray);

        Bundle innerObjBundle = bundle.getBundle(innerObjKey);
        assertNotNull(innerObjBundle);
        assertEquals(innerJsonObject.length(), innerObjBundle.size());
        assertTrue(innerObjBundle.containsKey(titleKey));
        assertEquals(innerJsonObject.getString(titleKey), innerObjBundle.getString(titleKey));
    }

    private static String[] toStringArray(JSONArray array) throws JSONException {
        int length = array.length();
        String[] strings = new String[length];
        for (int i = 0; i < length; i++) {
            strings[i] = array.get(i).toString();
        }
        return strings;
    }
}
