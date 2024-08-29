package com.projekt.exceptions;

public class ResourceHasAssignedItemsException extends RuntimeException {
  public ResourceHasAssignedItemsException(String resource, String item) {
    super("You cannot remove a " + resource + " if it has a " + item + " assigned to it");
  }
}
