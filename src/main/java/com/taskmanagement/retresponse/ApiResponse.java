package com.taskmanagement.retresponse;

import lombok.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponse extends ResponseEntity<ApiResponse.Payload> {

    public ApiResponse(HttpStatus status) {
        super(new Payload(status.value(), status.getReasonPhrase(), null), status);
    }

    public ApiResponse(HttpStatus status, Object data) {
        super(new Payload(status.value(), status.getReasonPhrase(), data), status);
    }

    public ApiResponse(int status, String message, HttpHeaders headers, Object data) {
        super(new Payload(status, message, data), headers, HttpStatus.OK);
    }

    public ApiResponse(int status, String message, HttpHeaders headers) {
        super(new Payload(status, message, null), headers, HttpStatus.OK);
    }


    @Value
    public static class Payload {
        int status;
        String error;
        Object data;
    }
}
