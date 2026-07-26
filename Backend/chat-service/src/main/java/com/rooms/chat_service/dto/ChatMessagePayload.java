package com.rooms.chat_service.dto;

import java.util.UUID;

public record ChatMessagePayload(
    String content,
    String type,
    String attachmentId
) {}
