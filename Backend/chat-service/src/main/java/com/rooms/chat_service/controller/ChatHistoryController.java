package com.rooms.chat_service.controller;

import com.rooms.chat_service.dto.ApiResponse;
import com.rooms.chat_service.entity.ChatMessage;
import com.rooms.chat_service.exception.UnauthorizedException;
import com.rooms.chat_service.repository.ChatMessageRepository;
import com.rooms.chat_service.service.RoomGrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/{roomId}")
public class ChatHistoryController {

    private final ChatMessageRepository chatMessageRepository;
    private final RoomGrpcClient roomGrpcClient;

    public ChatHistoryController(ChatMessageRepository chatMessageRepository, RoomGrpcClient roomGrpcClient) {
        this.chatMessageRepository = chatMessageRepository;
        this.roomGrpcClient = roomGrpcClient;
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getChatHistory(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        if (!roomGrpcClient.isUserMemberOfRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not a member of the room");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<ChatMessage> messagesPage = chatMessageRepository.findByRoomIdOrderBySentAtDesc(roomId, pageRequest);
        
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                200, 
                messagesPage.getContent(), 
                "Chat history fetched successfully"
        ));
    }
}
