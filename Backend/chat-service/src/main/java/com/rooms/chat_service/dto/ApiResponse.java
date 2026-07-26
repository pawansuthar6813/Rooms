package com.rooms.chat_service.dto;

public record ApiResponse<T>(
    boolean success,
    int status,
    T data,
    String message
) {}
