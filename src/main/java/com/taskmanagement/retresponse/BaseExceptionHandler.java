package com.taskmanagement.retresponse;

import com.taskmanagement.ex.ResourceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
public abstract class BaseExceptionHandler {

    public ApiResponse handleResourceException(ResourceException e) {
        log.error("Lỗi nghiệp vụ: {}", e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ApiResponse(e.getStatus(), e.getError(), headers);
    }

    public ErrorResponse handleValidationError(MethodArgumentNotValidException e) {
        log.error("Lỗi validate: {}", e.getBindingResult().getFieldErrors());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ErrorResponse(e.getBindingResult().getFieldErrors(), headers);
    }

    public ApiResponse handleAllException(Exception e) {
        log.error("Lỗi logic code: {}", e.getMessage(), e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ApiResponse(500, "Lỗi hệ thống. Vui lòng thử lại sau.", headers);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse handleEnumConversion(MethodArgumentTypeMismatchException e) {
        log.error("Invalid enum value for parameter '{}': {}", e.getName(), e.getValue());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ApiResponse(400, "Invalid status", headers);
    }
}
