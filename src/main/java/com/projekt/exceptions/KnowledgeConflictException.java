package com.projekt.exceptions;

public class KnowledgeConflictException extends RuntimeException {
  public KnowledgeConflictException(String knowledgeTitle, Long softwareID) {
    super("Knowledge with title '" + knowledgeTitle + "' and software ID '" + softwareID + "' already exists.");
  }
}
