package com.taskmanagement.ex;

import lombok.Getter;

@Getter
public class AppException extends ResourceException {
    private final ExceptionCode code;

    public AppException(ExceptionCode code) {
        super(code.getStatus(), code.getError());
        this.code = code;
    }

    public AppException(ExceptionCode code, Object... args) {
        super(code.getStatus(), String.format(code.getError(), args));
        this.code = code;
    }

}
