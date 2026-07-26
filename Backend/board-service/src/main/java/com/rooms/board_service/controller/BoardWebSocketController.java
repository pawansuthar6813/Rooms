package com.rooms.board_service.controller;

import com.rooms.board_service.dto.CanvasUpdatePayload;
import com.rooms.board_service.service.CanvasStateService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class BoardWebSocketController {

    private final CanvasStateService canvasStateService;

    public BoardWebSocketController(CanvasStateService canvasStateService) {
        this.canvasStateService = canvasStateService;
    }

    @MessageMapping("/board/{topicId}/update")
    public void handleBoardUpdate(@DestinationVariable String topicId, 
                                  CanvasUpdatePayload payload,
                                  SimpMessageHeaderAccessor headerAccessor) {
        // We could extract userId from headers if we needed to validate permissions per message,
        // but typically being subscribed to the topic implies access if we verified it on subscription.
        // For now, process the update.
        canvasStateService.processUpdate(topicId, payload);
    }
}
