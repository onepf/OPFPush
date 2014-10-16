package org.onepf.opfpush.baidu;

import android.content.Context;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;

import java.util.List;

/**
 * Push messaging receiver. Please write your callback function needs, in general:
 * onBind is necessary to deal with startWork return value;
 * onMessage to receive pass-through messages; onSetTags, onDelTags, onListTags callback tag related operations;
 * onNotificationClicked callback notification is clicked;
 * onUnbind is stopWork the return value of the callback interface
 * <p/>
 * The return value of the errorCode, explained as follows:
 * 0 - Success
 * 10001 - Network Problem
 * 30600 - Internal Server Error
 * 30601 - Method Not Allowed
 * 30602 - Request Params Not Valid
 * 30603 - Authentication Failed
 * 30604 - Quota Use Up Payment Required
 * 30605 - Data Required Not Found
 * 30606 - Request Time Expires Timeout
 * 30607 - Channel Token Timeout
 * 30608 - Bind Relation Not Found
 * 30609 - Bind Number Too Many
 * <p/>
 * When you encounter the above error is returned, if you can not explain your problem,
 * please return value with the same request and errorCode requestId Contact us track down the problem
 */
public class MyPushMessageReceiver extends FrontiaPushMessageReceiver {
    /**
     * TAG to Log
     */
    public static final String TAG = MyPushMessageReceiver.class.getSimpleName();

    /**
     * After calling PushManager.startWork, sdk will push
     * Bind server to initiate a request, this process is asynchronous.
     * The results returned by the binding request onBind.
     * If you need to use unicast push, we need to get the channel here
     * id and user id uploaded to the application server,
     * and then call the server interface with the channel id and user id to a single mobile phone or the user push.
     *
     * @param context   BroadcastReceiver execution Context
     * @param errorCode Binding interface return value, 0 - success
     * @param appid     Application id. errorCode non 0 to null
     * @param userId    Application user id. errorCode non 0 to null
     * @param channelId Application channel id. errorCode non 0  to null
     * @param requestId Initiated the request to the server id. Useful in tracing the problem;
     */
    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        // Binding is successful, set bound flag, can effectively reduce unnecessary binding request
        if (errorCode == PushConstants.ERROR_SUCCESS) {
            new Settings(context).saveBind(true);
        }
    }

    /**
     * Receive messages pass through function.
     *
     * @param message             Push messages
     * @param customContentString Custom content, or json string is empty
     */
    @Override
    public void onMessage(Context context, String message,
                          String customContentString) {
    }

    /**
     * Click function of the received notification. Note:
     * Push notifications are a user clicks on the front,
     * the application can not obtain the contents of the notification by the interface.
     *
     * @param title               Push notification title
     * @param description         Push notification description
     * @param customContentString Custom content, or json string is empty
     */
    @Override
    public void onNotificationClicked(Context context, String title,
                                      String description, String customContentString) {
    }

    /**
     * setTags() callback function
     *
     * @param errorCode   Error code. 0 indicates that some tag has been set successfully;
     *                    non 0 means that all tag settings have failed.
     * @param successTags Set successful tag
     * @param failTags    Set failed tag
     * @param requestId   Assigned to the request for cloud push id
     */
    @Override
    public void onSetTags(Context context, int errorCode,
                          List<String> successTags, List<String> failTags, String requestId) {
    }

    /**
     * delTags() callback function.
     *
     * @param errorCode   Error code. 0 indicates that some tag has been removed successfully;
     *                    non 0 means that all tag are deleted failed.
     * @param successTags Successfully removed the tag
     * @param failTags    Remove the failed tag
     * @param requestId   Assigned to the request for cloud push id
     */
    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> successTags, List<String> failTags, String requestId) {
    }

    /**
     * listTags() callback function.
     *
     * @param errorCode Error code. 0 indicates a successful tag list; non-zero indicates failure.
     * @param tags      All tag current application settings.
     * @param requestId Assigned to the request for cloud push id
     */
    @Override
    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {
    }

    /**
     * PushManager.stopWork() back to the letter.
     *
     * @param errorCode Error code. 0 indicates success from the cloud push unbinding; non-zero indicates failure.
     * @param requestId Assigned to the request for cloud push id
     */
    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        // Unbinding success, setting unbound flag,
        if (errorCode == PushConstants.ERROR_SUCCESS) {
            new Settings(context).saveBind(false);
        }
    }
}
