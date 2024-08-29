package com.projekt.exceptions;

public class UsernameOrEmailAlreadyExistsException extends RuntimeException {
    public UsernameOrEmailAlreadyExistsException(String username, String email) {
      super("Username '" + username + "' or Email '" + email + "' already exists.");
    }
}
