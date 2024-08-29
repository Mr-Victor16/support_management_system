package com.projekt.exceptions;

public class DefaultAdminAccountDeletionException extends RuntimeException {
    public DefaultAdminAccountDeletionException() {
      super("Default administrator account cannot be deleted.");
    }
}
