package org.onepf.opfpush.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.onepf.opfpush.model.Message;
import org.onepf.opfutils.OPFLog;
import org.onepf.opfutils.OPFUtils;

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
    protected void onHandleIntent(final Intent intent) {
        OPFLog.methodD(SendMessageService.class, "onHandleIntent", OPFUtils.toString(intent));

        if (ACTION_SEND_MESSAGE.equals(intent.getAction())) {
            final Message message = intent.getParcelableExtra(EXTRA_MESSAGE);
            final String messageTo = intent.getStringExtra(EXTRA_MESSAGES_TO);

            try {
                GoogleCloudMessaging.getInstance(this).send(
                        messageTo,
                        message.getId(),
                        message.getTimeToLeave(),
                        message.getData()
                );
                OPFLog.d("Message '%s' has sent.", message);
            } catch (IOException ex) {
                OPFLog.e(String.format("Error while send Message '%s'.", message), ex);
            }
        } else {
            OPFLog.w("Unknown action '%s'.", intent.getAction());
        }
    }
}
