package com.projekt.exceptions;

public class DefaultEntityDeletionException extends RuntimeException {
    private DefaultEntityDeletionException(String message) {
        super(message);
    }

    public static DefaultEntityDeletionException forDefaultAdmin() {
        return new DefaultEntityDeletionException("Default administrator account cannot be deleted.");
    }

    public static DefaultEntityDeletionException forDefaultStatus() {
        return new DefaultEntityDeletionException("Default ticket status cannot be deleted.");
    }
}
