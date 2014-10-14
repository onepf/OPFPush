package org.onepf.opfpush;

import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Kirill Rozov
 * @since 10/14/14.
 */
@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class MessageTest {

    @Test
    public void testCreate() {
        String msgId = "1";
        Message message = new Message(msgId, new Bundle(0));
        assertEquals(msgId, message.getId());
        assertEquals(0, message.getTimeToLeave());
        Bundle data = message.getData();
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    public void testCreateWithTTL() {
        String msgId = "1";
        long timeToLeave = TimeUnit.HOURS.toMillis(24);
        Message message = new Message(msgId, new Bundle(0), timeToLeave);
        assertEquals(msgId, message.getId());
        assertEquals(timeToLeave, message.getTimeToLeave());
        Bundle data = message.getData();
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }
}
