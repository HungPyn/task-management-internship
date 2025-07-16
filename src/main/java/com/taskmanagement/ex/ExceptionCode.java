package com.taskmanagement.ex;


import lombok.Getter;

@Getter
public enum ExceptionCode {

    USER_NOT_FOUND(1001, "User not found"),
    PASSWORD_INCORRECT(1002, "Password incorrect"),
    USER_FOUND(1003, "Users have existed"),

    UNAUTHORIZED(2003, "Unauthorized"),

    USER_ID_REQUIRED(3001,"User ID is required."),
    TASK_NOT_FOUND(3002,"Task not found");
    private final int status;
    private final String error;

    ExceptionCode(int status, String error) {
        this.status = status;
        this.error = error;
    }
}
