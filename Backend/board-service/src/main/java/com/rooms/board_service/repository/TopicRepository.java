package com.rooms.board_service.repository;

import com.rooms.board_service.entity.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
    List<Topic> findByRoomId(UUID roomId);
}
