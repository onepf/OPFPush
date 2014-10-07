package org.onepf.opfpush.gcm;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Kirill Rozov
 * @since 24.09.14.
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({
        GCMConstants.ERROR_AUTHENTICATION_FAILED,
        GCMConstants.ERROR_SERVICE_NOT_AVAILABLE
})
@interface GCMError {
}
