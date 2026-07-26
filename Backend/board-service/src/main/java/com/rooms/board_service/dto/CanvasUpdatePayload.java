package com.rooms.board_service.dto;

public record CanvasUpdatePayload(
    String topicId,
    long version,
    CanvasOperation operation
) {}
