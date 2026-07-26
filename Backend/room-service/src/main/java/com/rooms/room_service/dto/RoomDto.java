package com.rooms.room_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import java.io.Serializable;

public record RoomDto(
    UUID id,
    String name,
    String description,
    UUID ownerId,
    LocalDateTime createdAt,
    List<RoomMemberDto> members
) implements Serializable {}
