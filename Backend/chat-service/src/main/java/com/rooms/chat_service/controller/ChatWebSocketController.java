package com.rooms.chat_service.controller;

import com.rooms.chat_service.dto.ChatMessagePayload;
import com.rooms.chat_service.dto.ChatTypingPayload;
import com.rooms.chat_service.entity.ChatMessage;
import com.rooms.chat_service.repository.ChatMessageRepository;
import com.rooms.chat_service.service.RoomGrpcClient;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomGrpcClient roomGrpcClient;

    public ChatWebSocketController(ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate, RoomGrpcClient roomGrpcClient) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.roomGrpcClient = roomGrpcClient;
    }

    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(@DestinationVariable UUID roomId,
                            ChatMessagePayload payload,
                            SimpMessageHeaderAccessor headerAccessor) {
        
        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (userId == null || username == null) {
            return; // Not authenticated properly
        }
        
        // Verify membership
        if (!roomGrpcClient.isUserMemberOfRoom(roomId, userId)) {
            return; // Not a member
        }
        
        ChatMessage message = new ChatMessage(
            roomId, 
            userId, 
            username, 
            payload.content(), 
            payload.type() != null ? payload.type() : "TEXT", 
            payload.attachmentId()
        );
        
        message = chatMessageRepository.save(message);
        
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
    }
    
    @MessageMapping("/chat/{roomId}/typing")
    public void typingIndicator(@DestinationVariable UUID roomId,
                                ChatTypingPayload payload,
                                SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) return;
        
        if (!roomGrpcClient.isUserMemberOfRoom(roomId, userId)) {
            return;
        }
        
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/typing", payload);
    }
}
