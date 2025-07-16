package com.taskmanagement.retresponse;

import lombok.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

public class ErrorResponse extends ApiResponse {

    public ErrorResponse(List<FieldError> errors, HttpHeaders headers) {
        super(HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu không hợp lệ",
                headers,
                new ApiError(errors.stream()
                        .map(e -> new Error(e.getField(), e.getRejectedValue(), e.getDefaultMessage()))
                        .collect(Collectors.toList())));
    }

    @Value
    public static class ApiError {
        List<Error> errors;
    }

    @Value
    public static class Error {
        String field;
        Object value;
        String message;
    }
}

