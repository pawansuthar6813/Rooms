package com.rooms.board_service.controller;

import com.rooms.board_service.dto.ApiResponse;
import com.rooms.board_service.dto.CreateTopicRequest;
import com.rooms.board_service.entity.Topic;
import com.rooms.board_service.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Topic>> createTopic(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID roomId,
            @Valid @RequestBody CreateTopicRequest request) {
        Topic topic = topicService.createTopic(roomId, request.name(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, HttpStatus.CREATED.value(), topic, "Topic created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Topic>>> getTopics(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID roomId) {
        List<Topic> topics = topicService.getTopics(roomId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), topics, "Topics fetched successfully"));
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<ApiResponse<Topic>> getTopic(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID roomId,
            @PathVariable String topicId) {
        Topic topic = topicService.getTopic(topicId, roomId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), topic, "Topic fetched successfully"));
    }

    @DeleteMapping("/{topicId}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID roomId,
            @PathVariable String topicId) {
        topicService.deleteTopic(topicId, roomId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Topic deleted successfully"));
    }
}
