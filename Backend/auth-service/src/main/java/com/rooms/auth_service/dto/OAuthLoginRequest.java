package com.rooms.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(@NotBlank String idToken) {}
