package org.onepf.openpush.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static org.onepf.openpush.OpenPushLog.LOGE;
import static org.onepf.openpush.OpenPushLog.LOGI;

/**
* Created by krozov on 22.09.14.
*/
class SendMessageTask extends AsyncTask<Void, Void, Void> {
    private static final String GCM_SENDER_SUFFIX = "@gcm.googleapis.com";

    private final String mTo;
    private final GCMMessage mMessage;
    private final WeakReference<Context> mContextRef;

    SendMessageTask(@NonNull Context context,
                    @NonNull String senderId,
                    @NonNull GCMMessage message) {
        mTo = senderId + GCM_SENDER_SUFFIX;
        mMessage = message;
        mContextRef = new WeakReference<Context>(context.getApplicationContext());
    }

    protected Void doInBackground(Void... params) {
        Context context = mContextRef.get();
        if (context != null) {
            try {
                GoogleCloudMessaging.getInstance(context)
                        .send(mTo, mMessage.getId(), mMessage.getTimeToLeave(), mMessage.getData());
                LOGI(String.format("Message '%s' has sent.", mMessage));
            } catch (IOException ex) {
                LOGE(String.format("Error while send Message '%s'.", mMessage), ex);
            }
        } else {
            LOGE(String.format("Error while send Message '%s'. No context.", mMessage));
        }
        return null;
    }
}
