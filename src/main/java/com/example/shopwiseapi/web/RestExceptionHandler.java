package com.example.shopwiseapi.web;

import com.example.shopwiseapi.service.ResourceAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleResourceAlreadyExists(ResourceAlreadyExistsException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    public record ErrorResponse(String message) {
    }
}
