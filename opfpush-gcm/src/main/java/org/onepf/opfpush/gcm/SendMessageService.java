package org.onepf.opfpush.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.Message;

import java.io.IOException;

import static org.onepf.opfpush.OPFPushLog.LOGE;
import static org.onepf.opfpush.OPFPushLog.LOGI;

/**
 * @author Kirill Rozov
 * @since 10/13/14.
 */
public class SendMessageService extends IntentService {

    public static final String ACTION_SEND_MESSAGE = "org.onepf.opfpush.gcm.SEND_MSG";
    public static final String EXTRA_MESSAGE = "msg";
    public static final String EXTRA_TO = "to";

    public SendMessageService() {
        super("SendMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_SEND_MESSAGE.equals(intent.getAction())) {
            Message msg = intent.getParcelableExtra(EXTRA_MESSAGE);
            String to = intent.getStringExtra(EXTRA_TO);
            try {
                GoogleCloudMessaging.getInstance(this)
                        .send(to, msg.getId(), msg.getTimeToLeave(), msg.getData());
                LOGI(String.format("Message '%s' has sent.", msg));
            } catch (IOException ex) {
                LOGE(String.format("Error while send Message '%s'.", msg), ex);
            }
        }
    }
}
