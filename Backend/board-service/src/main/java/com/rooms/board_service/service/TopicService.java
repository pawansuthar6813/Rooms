package com.rooms.board_service.service;

import com.rooms.board_service.entity.Topic;
import com.rooms.board_service.exception.ResourceNotFoundException;
import com.rooms.board_service.exception.UnauthorizedException;
import com.rooms.board_service.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final RoomGrpcClient roomGrpcClient;
    private final CanvasStateService canvasStateService;

    public TopicService(TopicRepository topicRepository, RoomGrpcClient roomGrpcClient, CanvasStateService canvasStateService) {
        this.topicRepository = topicRepository;
        this.roomGrpcClient = roomGrpcClient;
        this.canvasStateService = canvasStateService;
    }

    public Topic createTopic(UUID roomId, String name, UUID userId) {
        verifyMembership(roomId, userId);
        Topic topic = new Topic(roomId, name, userId);
        return topicRepository.save(topic);
    }

    public List<Topic> getTopics(UUID roomId, UUID userId) {
        verifyMembership(roomId, userId);
        List<Topic> topics = topicRepository.findByRoomId(roomId);
        for (Topic topic : topics) {
            java.util.Map<String, Object> latestState = canvasStateService.getLatestState(topic.getId());
            if (latestState != null) {
                topic.setCanvasState(latestState);
            }
        }
        return topics;
    }

    public Topic getTopic(String topicId, UUID roomId, UUID userId) {
        verifyMembership(roomId, userId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        if (!topic.getRoomId().equals(roomId)) {
            throw new ResourceNotFoundException("Topic does not belong to this room");
        }
        java.util.Map<String, Object> latestState = canvasStateService.getLatestState(topicId);
        if (latestState != null) {
            topic.setCanvasState(latestState);
        }
        return topic;
    }

    public void deleteTopic(String topicId, UUID roomId, UUID userId) {
        if (!roomGrpcClient.isUserOwnerOfRoom(roomId, userId)) {
            throw new UnauthorizedException("Only the room owner can delete a topic");
        }
        Topic topic = getTopic(topicId, roomId, userId);
        topicRepository.delete(topic);
    }

    private void verifyMembership(UUID roomId, UUID userId) {
        if (!roomGrpcClient.isUserMemberOfRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not a member of the room");
        }
    }
}
