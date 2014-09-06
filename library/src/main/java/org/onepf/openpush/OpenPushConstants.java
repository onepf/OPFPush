package org.onepf.openpush;

/**
 * Created by krozov on 06.09.14.
 */
public interface OpenPushConstants {
    int NO_ERROR = -1;
    int ERROR_SERVICE_NOT_AVAILABLE = 1;
    int ERROR_INVALID_PARAMETERS = 2;
    int ERROR_INVALID_SENDER = 3;
    int ERROR_UNKNOWN = 4;
    int ERROR_AUTHEFICATION_FAILED = 5;

    String ACTION_REGISTERED = "org.onepf.openpush.gcm.ACTION.registered";
    String ACTION_UNREGISTERED = "org.onepf.openpush.gcm.ACTION.unregistered";
    String ACTION_MESSAGE = "org.onepf.openpush.gcm.ACTION.message";
    String ACTION_MESSAGE_DELETED = "org.onepf.openpush.gcm.ACTION.message_deleted";
    String ACTION_HOST_APP_REMOVED= "org.onepf.openpush.gcm.ACTION.host_app_removed";

    String EXTRA_PROVIDER_NAME = "org.onepf.openpush.EXTRA.provider_name";
    String EXTRA_REGISTRATION_ID = "org.onepf.openpush.EXTRA.registration_id";
    String EXTRA_MESSAGES_COUNT = "org.onepf.openpush.EXTRA.messages_count";
    String EXTRA_HOST_APP_PACKAGE = "org.onepf.openpush.EXTRA.host_app_package";
}
