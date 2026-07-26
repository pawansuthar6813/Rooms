package com.rooms.room_service.dto;

public record ApiResponse<T>(
    boolean success,
    int statusCode,
    T data,
    String message
) {}
