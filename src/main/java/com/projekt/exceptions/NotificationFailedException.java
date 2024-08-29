package com.projekt.exceptions;

public class NotificationFailedException extends RuntimeException {
    public NotificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
