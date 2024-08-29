package com.projekt.exceptions;

public class NameConflictException extends RuntimeException {
  public NameConflictException(String resource, String name) {
    super(resource + " with name '" + name + "' already exists.");
  }
}
