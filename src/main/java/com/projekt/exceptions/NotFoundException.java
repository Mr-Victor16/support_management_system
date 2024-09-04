package com.projekt.exceptions;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String resource, Long id) {
    super(resource + " with ID " + id + " not found.");
  }

  public NotFoundException(String resource, String name) {
    super(resource + " with name '" + name + "' not found.");
  }

  public NotFoundException(String resource) {
    super("Not found " + resource);
  }
}
