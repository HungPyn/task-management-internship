package com.taskmanagement.enums;

public enum Status {
    PENDING,
    IN_PROGRESS,
    COMPLETED;

    public static Status fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> Status.PENDING;
            case "IN_PROGRESS" -> Status.IN_PROGRESS;
            case "COMPLETED" -> Status.COMPLETED;
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        };
    }
}
