package com.rooms.auth_service.dto;

import java.util.UUID;

public record UserInfoResponse(UUID id, String email, String username) {}
