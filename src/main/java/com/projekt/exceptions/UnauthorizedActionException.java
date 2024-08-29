package com.projekt.exceptions;

public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }

    public static UnauthorizedActionException forActionToResource(String action, String resource) {
        return new UnauthorizedActionException("You do not have permission to " + action + " to this " + resource);
    }

    public static UnauthorizedActionException forActionOnResource(String action, String resource) {
        return new UnauthorizedActionException("You do not have permission to " + action + " this " + resource);
    }
}
