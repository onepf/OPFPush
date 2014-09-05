package org.onepf.openpush.exception;

/**
 * Created by krozov on 05.09.14.
 */
public class RegistrationException extends Exception {
    public RegistrationException() {
    }

    public RegistrationException(String detailMessage) {
        super(detailMessage);
    }

    public RegistrationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RegistrationException(Throwable throwable) {
        super(throwable);
    }
}
