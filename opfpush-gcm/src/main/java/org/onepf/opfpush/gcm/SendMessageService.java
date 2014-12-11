package org.onepf.opfpush.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import junit.framework.Assert;

import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.model.Message;

import java.io.IOException;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 10/13/14.
 */
public class SendMessageService extends IntentService {

    public static final String ACTION_SEND_MESSAGE = "org.onepf.opfpush.gcm.SEND_MSG";
    public static final String EXTRA_MESSAGE = "msg";
    public static final String EXTRA_MESSAGES_TO = "to";

    public SendMessageService() {
        super("SendMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        OPFPushLog.methodD(SendMessageService.class, "onHandleIntent", intent);

        if (ACTION_SEND_MESSAGE.equals(intent.getAction())) {
            final Message message = intent.getParcelableExtra(EXTRA_MESSAGE);
            Assert.assertNotNull(message);
            final String messageTo = intent.getStringExtra(EXTRA_MESSAGES_TO);
            Assert.assertNotNull(messageTo);

            try {
                GoogleCloudMessaging.getInstance(this)
                        .send(messageTo, message.getId(), message.getTimeToLeave(), message.getData());
                OPFPushLog.d("Message '%s' has sent.", message);
            } catch (IOException ex) {
                OPFPushLog.e(String.format("Error while send Message '%s'.", message), ex);
            }
        } else {
            OPFPushLog.w("Unknown action '%s'.", intent.getAction());
        }
    }
}
