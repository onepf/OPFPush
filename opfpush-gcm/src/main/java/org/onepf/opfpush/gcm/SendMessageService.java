package org.onepf.opfpush.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import junit.framework.Assert;

import org.onepf.opfpush.Message;

import java.io.IOException;

import static org.onepf.opfpush.OPFPushLog.LOGE;
import static org.onepf.opfpush.OPFPushLog.LOGI;
import static org.onepf.opfpush.OPFPushLog.LOGW;

/**
 * @author Kirill Rozov
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
        if (ACTION_SEND_MESSAGE.equals(intent.getAction())) {
            Message msg = intent.getParcelableExtra(EXTRA_MESSAGE);
            Assert.assertNotNull(msg);
            String msgTo = intent.getStringExtra(EXTRA_MESSAGES_TO);
            Assert.assertNotNull(msgTo);

            try {
                GoogleCloudMessaging.getInstance(this)
                        .send(msgTo, msg.getId(), msg.getTimeToLeave(), msg.getData());
                LOGI("Message '%s' has sent.", msg);
            } catch (IOException ex) {
                LOGE(String.format("Error while send Message '%s'.", msg), ex);
            }
        } else {
            LOGW("Unknown action '%s'.", intent.getAction());
        }
    }
}
