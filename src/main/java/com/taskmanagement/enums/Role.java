package com.taskmanagement.enums;

public enum Role {
    ADMIN, USER;

    public static Role fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        return switch (role.trim().toUpperCase()) {
            case "ADMIN" -> Role.ADMIN;
            case "USER" -> Role.USER;
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}
