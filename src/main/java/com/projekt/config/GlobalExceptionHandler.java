package com.projekt.config;

import com.projekt.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(KnowledgeConflictException.class)
    @ResponseBody
    public String handleKnowledgeConflictException(KnowledgeConflictException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyActivatedException.class)
    @ResponseBody
    public String handleUserAlreadyActivatedException(UserAlreadyActivatedException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UsernameOrEmailAlreadyExistsException.class)
    @ResponseBody
    public String handleUsernameOrEmailAlreadyExistsException(UsernameOrEmailAlreadyExistsException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(DefaultEntityDeletionException.class)
    @ResponseBody
    public String handleDefaultAdminAccountDeletionException(DefaultEntityDeletionException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidLoginException.class)
    @ResponseBody
    public String handleInvalidLoginException(InvalidLoginException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NotificationFailedException.class)
    @ResponseBody
    public String handleNotificationFailedException(NotificationFailedException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UnauthorizedActionException.class)
    @ResponseBody
    public String handleUnauthorizedActionException(UnauthorizedActionException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FileProcessingException.class)
    @ResponseBody
    public String handleFileProcessingException(FileProcessingException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NameConflictException.class)
    @ResponseBody
    public String handleNameConflictException(NameConflictException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public String handleNotFoundException(NotFoundException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ResourceHasAssignedItemsException.class)
    @ResponseBody
    public String handleResourceHasAssignedItemsException(ResourceHasAssignedItemsException ex) {
        return ex.getMessage();
    }
}
