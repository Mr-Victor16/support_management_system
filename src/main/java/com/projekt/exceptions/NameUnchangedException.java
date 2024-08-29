package com.projekt.exceptions;

public class NameUnchangedException extends RuntimeException {
  public NameUnchangedException(String resource, String name) {
    super(resource + " name is the same as the current name '" + name + "'");
  }
}
