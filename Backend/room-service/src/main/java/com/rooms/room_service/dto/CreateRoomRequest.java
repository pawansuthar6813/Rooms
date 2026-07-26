package com.rooms.room_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
    @NotBlank(message = "Room name is required")
    @Size(max = 255, message = "Room name must be less than 255 characters")
    String name,
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    String description
) {}
