package com.rooms.auth_service.dto;

public record ApiResponse<T>(
    boolean success,
    int statusCode,
    T data,
    String message
) {}
