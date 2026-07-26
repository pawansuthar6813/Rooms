package com.rooms.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupUsernameRequest(@NotBlank String username) {}
