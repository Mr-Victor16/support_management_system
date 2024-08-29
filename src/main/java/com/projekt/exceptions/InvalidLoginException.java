package com.projekt.exceptions;

public class InvalidLoginException extends RuntimeException {
  public InvalidLoginException() {
    super("Incorrect login details");
  }
}
