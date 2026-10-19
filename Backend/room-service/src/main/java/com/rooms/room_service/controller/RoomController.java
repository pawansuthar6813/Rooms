package com.rooms.room_service.controller;

import com.rooms.room_service.dto.ApiResponse;
import com.rooms.room_service.dto.CreateRoomRequest;
import com.rooms.room_service.dto.RoomDto;
import com.rooms.room_service.entity.MemberRole;
import com.rooms.room_service.service.RoomService;
import com.rooms.room_service.dto.PendingInviteDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(@RequestHeader("X-User-Id") UUID userId,
                                                           @Valid @RequestBody CreateRoomRequest request) {
        RoomDto room = roomService.createRoom(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, HttpStatus.CREATED.value(), room, "Room created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomDto>>> getUserRooms(@RequestHeader("X-User-Id") UUID userId) {
        List<RoomDto> rooms = roomService.getUserRooms(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), rooms, "Rooms fetched successfully"));
    }

    @GetMapping("/invites")
    public ResponseEntity<ApiResponse<List<PendingInviteDto>>> getPendingInvites(@RequestHeader("X-User-Id") UUID userId) {
        List<PendingInviteDto> invites = roomService.getPendingInvites(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), invites, "Invites fetched successfully"));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomDto>> getRoomDetails(@RequestHeader("X-User-Id") UUID userId,
                                                               @PathVariable UUID roomId) {
        RoomDto room = roomService.getRoomDetails(roomId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), room, "Room details fetched successfully"));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@RequestHeader("X-User-Id") UUID userId,
                                                        @PathVariable UUID roomId) {
        roomService.deleteRoom(roomId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Room deleted successfully"));
    }

    @PostMapping("/{roomId}/invite")
    public ResponseEntity<ApiResponse<Void>> sendInvite(@RequestHeader("X-User-Id") UUID userId,
                                                        @PathVariable UUID roomId,
                                                        @RequestParam String email) {
        roomService.generateAndSendInvite(roomId, email, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Invite sent successfully"));
    }

    @PostMapping("/join/{inviteToken}")
    public ResponseEntity<ApiResponse<Void>> joinRoom(@RequestHeader("X-User-Id") UUID userId,
                                                      @PathVariable UUID inviteToken) {
        roomService.acceptInvite(inviteToken, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Joined room successfully"));
    }

    @DeleteMapping("/{roomId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@RequestHeader("X-User-Id") UUID requesterId,
                                                          @PathVariable UUID roomId,
                                                          @PathVariable UUID memberId) {
        roomService.removeMember(roomId, memberId, requesterId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Member removed successfully"));
    }

    @PatchMapping("/{roomId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<Void>> changeRole(@RequestHeader("X-User-Id") UUID requesterId,
                                                        @PathVariable UUID roomId,
                                                        @PathVariable UUID memberId,
                                                        @RequestParam MemberRole role) {
        roomService.changeRole(roomId, memberId, role, requesterId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Role changed successfully"));
    }
}
