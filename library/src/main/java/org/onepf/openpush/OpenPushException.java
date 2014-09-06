package org.onepf.openpush;

/**
 * Created by krozov on 05.09.14.
 */
public class OpenPushException extends RuntimeException {
    public OpenPushException() {
    }

    public OpenPushException(String detailMessage) {
        super(detailMessage);
    }

    public OpenPushException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public OpenPushException(Throwable throwable) {
        super(throwable);
    }
}
