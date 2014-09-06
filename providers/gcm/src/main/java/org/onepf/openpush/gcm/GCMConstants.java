package org.onepf.openpush.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by krozov on 06.09.14.
 */
class GCMConstants {
    static final String ACTION_REGISTRATION = "org.onepf.openpush.gcm.registration";
    static final String ACTION_UNREGISTRATION = "org.onepf.openpush.gcm.unregistration";
    static final String ACTION_ERROR = "org.onepf.openpush.gcm.error";

    static final String EXTRA_ERROR_ID = "org.onepf.openpush.gcm.error_id";
    static final String EXTRA_TOKEN = "org.onepf.openpush.gcm.token";

    static final String ERROR_AUTHEFICATION_FAILED = "ERROR_AUTHEFICATION_FAILED";
    static final String ERROR_SERVICE_NOT_AVAILABLE = GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE;
}
