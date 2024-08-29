package com.projekt.exceptions;

public class FileProcessingException extends RuntimeException {
  public FileProcessingException(String fileName, Throwable cause) {
    super("Failed to process file: " + fileName + ", cause: " + cause);
  }
}
