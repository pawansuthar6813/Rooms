package com.rooms.room_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingInviteDto(
    UUID token,
    String roomName,
    String invitedBy, // Optional: if we want to show who invited them, though currently RoomInvite doesn't store this.
    LocalDateTime expiresAt
) {}
