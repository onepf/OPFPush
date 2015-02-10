package org.onepf.opfpush;

import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.model.Message;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 10/14/14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class MessageTest {

    @Test
    public void testCreate() {
        final String msgId = "1";
        final Message message = new Message(msgId, new Bundle(0));

        assertEquals(msgId, message.getId());
        assertEquals(0, message.getTimeToLeave());

        final Bundle data = message.getData();
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    public void testCreateWithTTL() {
        final String msgId = "1";
        final long timeToLeave = TimeUnit.HOURS.toMillis(24);
        final Message message = new Message(msgId, new Bundle(0), timeToLeave);

        assertEquals(msgId, message.getId());
        assertEquals(timeToLeave, message.getTimeToLeave());

        final Bundle data = message.getData();
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    public void testCreateWithData() {
        final String msgId = "1";
        final String dataValue = "DATA_VALUE";
        final String dataKey = "DATA_KEY";
        final Bundle data = new Bundle();
        data.putString(dataKey, dataValue);
        final Message message = new Message(msgId, data);

        assertEquals(msgId, message.getId());
        assertEquals(0, message.getTimeToLeave());

        final Bundle actualData = message.getData();
        assertNotNull(actualData);
        assertFalse(actualData.isEmpty());

        final String actualDataValue = actualData.getString(dataKey);
        assertEquals(dataValue, actualDataValue);
    }
}
