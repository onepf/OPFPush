package org.onepf.opfpush.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import static org.onepf.opfpush.OPFPushLog.LOGE;
import static org.onepf.opfpush.OPFPushLog.LOGI;

/**
 * Task for send message from client to server.
 *
 * @author Kirill Rozov
 * @since 22.09.14.
 */
class SendMessageTask extends AsyncTask<Void, Void, Void> {
    private static final String GCM_SENDER_SUFFIX = "@gcm.googleapis.com";

    private final String mTo;
    private final GCMMessage mMessage;
    private final Context mContext;

    SendMessageTask(@NonNull Context context,
                    @NonNull String senderId,
                    @NonNull GCMMessage message) {
        mTo = senderId + GCM_SENDER_SUFFIX;
        mMessage = message;
        mContext = context.getApplicationContext();
    }

    protected Void doInBackground(Void... params) {
        try {
            if (!isCancelled()) {
                GoogleCloudMessaging.getInstance(mContext)
                        .send(mTo, mMessage.getId(), mMessage.getTimeToLeave(), mMessage.getData());
                LOGI(String.format("Message '%s' has sent.", mMessage));
            }
        } catch (IOException ex) {
            LOGE(String.format("Error while send Message '%s'.", mMessage), ex);
        }
        return null;
    }
}
