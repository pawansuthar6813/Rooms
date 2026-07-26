package com.rooms.board_service.dto;

import java.util.Map;

public record CanvasOperation(
    String type,
    String elementId,
    Map<String, Object> data
) {}
