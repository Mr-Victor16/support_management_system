package com.projekt.exceptions;

public class UserAlreadyActivatedException extends RuntimeException {
    public UserAlreadyActivatedException(Long userID) {
        super("User with ID '" + userID + "' is already activated.");
    }
}
