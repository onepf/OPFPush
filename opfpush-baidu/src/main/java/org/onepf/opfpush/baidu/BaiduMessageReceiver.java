package org.onepf.opfpush.baidu;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;

import org.json.JSONException;
import org.onepf.opfpush.*;

import java.util.List;

import org.onepf.opfpush.Error;

import static org.onepf.opfpush.OPFPushLog.LOGE;

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
public class BaiduMessageReceiver extends FrontiaPushMessageReceiver {

    /**
     * After calling PushManager.startWork, sdk will push
     * Bind server to initiate a request, this process is asynchronous.
     * The results returned by the binding request onBind.
     * If you need to use unicast push, we need to get the channel here
     * id and user id uploaded to the application server,
     * and then call the server interface with the channel id and user id to a single mobile phone or the user push.
     *
     * @param context   BroadcastReceiver execution Context
     * @param errorCode Binding interface return value, {@link PushConstants#ERROR_SUCCESS} - success
     * @param appid     Application id. Null when not success
     * @param userId    Application user id. Null when not success
     * @param channelId Application channel id. Null when not success
     * @param requestId Initiated the request to the server id. Useful in tracing the problem;
     */
    @Override
    public void onBind(@NonNull Context context,
                       @ErrorCode int errorCode,
                       @Nullable String appid,
                       @Nullable String userId,
                       @Nullable String channelId,
                       @NonNull String requestId) {
        // Binding is successful, set bound flag, can effectively reduce unnecessary binding request
        final Result result;
        if (errorCode == PushConstants.ERROR_SUCCESS) {
            new Settings(context).saveBind(true);
            result = Result.success(BaiduPushProvider.NAME, null, Result.Type.REGISTRATION);
        } else {
            result = Result.error(BaiduPushProvider.NAME,
                    convertError(errorCode), Result.Type.REGISTRATION);
        }
        OPFPushHelper.getInstance(context).getProviderCallback().onResult(result);
    }

    private static Error convertError(@ErrorCode int error) {
        switch (error) {
            case PushConstants.ERROR_AUTHENTICATION_FAILED:
                return Error.AUTHENTICATION_FAILED;

            case PushConstants.ERROR_NETWORK_ERROR:
            case PushConstants.ERROR_TIME_EXPIRES:
            case PushConstants.ERROR_CHANNEL_TOKEN_TIMEOUT:
            case PushConstants.ERROR_SERVICE_NOT_AVAILABLE:
            case PushConstants.ERROR_SERVICE_NOT_AVAILABLE_TEMP:
                return Error.SERVICE_NOT_AVAILABLE;

            case PushConstants.ERROR_METHOD_ERROR:
            case PushConstants.ERROR_PARAMS_ERROR:
                return Error.INVALID_PARAMETERS;

            case PushConstants.ERROR_SUCCESS:
                return null;

            default:
                return Error.UNKNOWN;
        }
    }

    /**
     * Receive messages pass through function.
     *
     * @param message             Push messages
     * @param customContentString Custom content, or json string is empty
     */
    @Override
    public void onMessage(@NonNull Context context,
                          @NonNull String message,
                          String customContentString) {
        try {
            Bundle bundle = Utils.messageToBundle(message);
            bundle.putString(BaiduConstants.EXTRA_CUSTOM_CONTENT_STRING, customContentString);
            OPFPushHelper.getInstance(context)
                    .getProviderCallback().onMessage(BaiduPushProvider.NAME, bundle);
        } catch (JSONException e) {
            LOGE("Error parse message '" + message + "'.", e);
        }
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
     * @param errorCode   Error code. {@link PushConstants#ERROR_SUCCESS}  indicates that some tag has been set successfully;
     *                    another values means that all tag settings have failed.
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
    public void onDelTags(Context context, @ErrorCode int errorCode,
                          List<String> successTags, List<String> failTags, String requestId) {
    }

    /**
     * listTags() callback function.
     *
     * @param errorCode Error code. {@link PushConstants#ERROR_SUCCESS} indicates a successful tag list; non-zero indicates failure.
     * @param tags      All tag current application settings.
     * @param requestId Assigned to the request for cloud push id
     */
    @Override
    public void onListTags(Context context, @ErrorCode int errorCode, List<String> tags, String requestId) {
    }

    /**
     * PushManager.stopWork() back to the letter.
     *
     * @param errorCode Error code. {@link PushConstants#ERROR_SUCCESS}  indicates success from the cloud push unbinding; non-zero indicates failure.
     * @param requestId Assigned to the request for cloud push id
     */
    @Override
    public void onUnbind(Context context, @ErrorCode int errorCode, String requestId) {
        // Unbinding success, setting unbound flag,
        final Result result;
        if (errorCode == PushConstants.ERROR_SUCCESS) {
            new Settings(context).saveBind(false);
            result = Result.success(BaiduPushProvider.NAME, null, Result.Type.UNREGISTRATION);
        } else {
            result = Result.error(BaiduPushProvider.NAME,
                    convertError(errorCode), Result.Type.UNREGISTRATION);
        }
        OPFPushHelper.getInstance(context).getProviderCallback().onResult(result);
    }
}
