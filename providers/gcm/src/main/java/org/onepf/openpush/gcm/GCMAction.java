package org.onepf.openpush.gcm;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Kirill Rozov
 * @since 24.09.14.
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({
        GCMConstants.ACTION_REGISTRATION,
        GCMConstants.ACTION_UNREGISTRATION
})
@interface GCMAction {
}
