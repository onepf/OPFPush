package org.onepf.opfpush.baidu;

import android.support.annotation.IntDef;

import com.baidu.android.pushservice.PushConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Kirill Rozov
 * @since 10/16/14.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        PushConstants.ERROR_AIDL_FAIL,
        PushConstants.ERROR_SUCCESS,
        PushConstants.ERROR_NETWORK_ERROR,
        PushConstants.ERROR_SERVICE_NOT_AVAILABLE,
        PushConstants.ERROR_SERVICE_NOT_AVAILABLE_TEMP,
        PushConstants.ERROR_UNKNOWN,
        PushConstants.ERROR_SERVER_INTERNAL_ERROR,
        PushConstants.ERROR_METHOD_ERROR,
        PushConstants.ERROR_PARAMS_ERROR,
        PushConstants.ERROR_AUTHENTICATION_FAILED,
        PushConstants.ERROR_QUOTA_USE_UP,
        PushConstants.ERROR_DATA_NOT_FOUND,
        PushConstants.ERROR_TIME_EXPIRES,
        PushConstants.ERROR_CHANNEL_TOKEN_TIMEOUT,
        PushConstants.ERROR_BIND_NOT_EXIST,
        PushConstants.ERROR_BIND_OVERLOAD
})
public @interface ErrorCode {
}
