package com.taskmanagement.retresponse;

import com.taskmanagement.ex.ResourceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(ResourceException.class)
    public ApiResponse handleResource(ResourceException e) {
        return super.handleResourceException(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        return super.handleValidationError(e);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse handleOther(Exception e) {
        return super.handleAllException(e);
    }
}
