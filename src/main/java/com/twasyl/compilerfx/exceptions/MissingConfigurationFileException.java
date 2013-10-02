package com.twasyl.compilerfx.exceptions;

public class MissingConfigurationFileException extends RuntimeException {

    public MissingConfigurationFileException() {
        super();
    }

    public MissingConfigurationFileException(String message) {
        super(message);
    }

    public MissingConfigurationFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingConfigurationFileException(Throwable cause) {
        super(cause);
    }

    protected MissingConfigurationFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
