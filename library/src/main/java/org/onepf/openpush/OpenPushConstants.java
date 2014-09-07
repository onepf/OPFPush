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

    String ACTION_REGISTERED = "org.onepf.openpush.registered";
    String ACTION_UNREGISTERED = "org.onepf.openpush.unregistered";
    String ACTION_MESSAGE = "org.onepf.openpush.message";
    String ACTION_DELETED_MESSAGES = "org.onepf.openpush.message_deleted";
    String ACTION_REGISTRATION_ERROR = "org.onepf.openpush.registration_error";
    String ACTION_NO_AVAILABLE_PROVIDER = "org.onepf.openpush.no_available_provider";
    String ACTION_HOST_APP_REMOVED = "org.onepf.openpush.host_app_removed";

    String EXTRA_PROVIDER_NAME = "org.onepf.openpush.provider_name";
    String EXTRA_REGISTRATION_ID = "org.onepf.openpush.registration_id";
    String EXTRA_ERROR_ID = "org.onepf.openpush.error_id";
    String EXTRA_MESSAGES_COUNT = "org.onepf.openpush.messages_count";
    String EXTRA_MESSAGE = "org.onepf.openpush.message";
    String EXTRA_HOST_APP_PACKAGE = "org.onepf.openpush.host_app_package";
}
