package com.rooms.chat_service.repository;

import com.rooms.chat_service.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    Page<ChatMessage> findByRoomIdOrderBySentAtDesc(UUID roomId, Pageable pageable);
}
