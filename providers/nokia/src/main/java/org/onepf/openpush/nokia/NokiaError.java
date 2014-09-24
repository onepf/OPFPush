package org.onepf.openpush.nokia;

import android.support.annotation.StringDef;

import com.nokia.push.PushConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by krozov on 24.09.14.
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({
        PushConstants.ERROR_INVALID_PARAMETERS,
        PushConstants.ERROR_INVALID_SENDER,
        PushConstants.ERROR_SERVICE_NOT_AVAILABLE
})
public @interface NokiaError {
}
