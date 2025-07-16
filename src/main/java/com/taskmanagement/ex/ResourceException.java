package com.taskmanagement.ex;

import lombok.Getter;

@Getter
public class ResourceException extends RuntimeException{

    private final int status;
    private final String error;

    public ResourceException(int status, String error) {
        super(error);
        this.status = status;
        this.error = error;
    }
}
