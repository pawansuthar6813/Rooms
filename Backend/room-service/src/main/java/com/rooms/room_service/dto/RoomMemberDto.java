package com.rooms.room_service.dto;

import com.rooms.room_service.entity.MemberRole;

import java.time.LocalDateTime;
import java.util.UUID;

import java.io.Serializable;

public record RoomMemberDto(
    UUID userId,
    String username,
    String email,
    MemberRole role,
    LocalDateTime joinedAt
) implements Serializable {}
