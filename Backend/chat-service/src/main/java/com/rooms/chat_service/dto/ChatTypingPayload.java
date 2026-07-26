package com.rooms.chat_service.dto;

import java.util.UUID;

public record ChatTypingPayload(
    UUID authorId,
    String authorUsername,
    boolean isTyping
) {}
