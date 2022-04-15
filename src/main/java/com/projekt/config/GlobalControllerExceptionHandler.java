package com.projekt.config;

import com.projekt.exceptions.TicketNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(TicketNotFoundException.class)
    public String handleConflict(){
        return "TicketNotFoundException";
    }
}
