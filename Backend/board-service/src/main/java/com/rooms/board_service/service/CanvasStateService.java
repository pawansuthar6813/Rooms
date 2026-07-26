package com.rooms.board_service.service;

import com.rooms.board_service.dto.CanvasUpdatePayload;
import com.rooms.board_service.entity.Topic;
import com.rooms.board_service.repository.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CanvasStateService {

    private static final Logger logger = LoggerFactory.getLogger(CanvasStateService.class);

    private final TopicRepository topicRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // topicId -> canvasState (map of elementId -> elementData)
    private final Map<String, Map<String, Object>> topicStates = new ConcurrentHashMap<>();
    
    // topicId -> lock
    private final Map<String, ReentrantLock> topicLocks = new ConcurrentHashMap<>();

    // Keep track of which topics have unsaved changes
    private final Set<String> dirtyTopics = ConcurrentHashMap.newKeySet();

    public CanvasStateService(TopicRepository topicRepository, SimpMessagingTemplate messagingTemplate) {
        this.topicRepository = topicRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void processUpdate(String topicId, CanvasUpdatePayload payload) {
        ReentrantLock lock = topicLocks.computeIfAbsent(topicId, k -> new ReentrantLock());
        lock.lock();
        try {
            // Load state into memory if not present
            Map<String, Object> state = topicStates.computeIfAbsent(topicId, this::loadStateFromDb);

            // Apply operation to in-memory state
            if (payload.operation() != null) {
                String type = payload.operation().type();
                String elementId = payload.operation().elementId();
                Map<String, Object> data = payload.operation().data();

                if ("SET".equalsIgnoreCase(type) || "ADD".equalsIgnoreCase(type) || "UPDATE".equalsIgnoreCase(type)) {
                    state.put(elementId, data);
                } else if ("REMOVE".equalsIgnoreCase(type) || "DELETE".equalsIgnoreCase(type)) {
                    state.remove(elementId);
                }
                
                dirtyTopics.add(topicId);
                
                // Broadcast to all subscribers
                messagingTemplate.convertAndSend("/topic/board/" + topicId, payload);
            }
        } finally {
            lock.unlock();
        }
    }

    private Map<String, Object> loadStateFromDb(String topicId) {
        return topicRepository.findById(topicId)
                .map(Topic::getCanvasState)
                .orElse(new ConcurrentHashMap<>());
    }

    public Map<String, Object> getLatestState(String topicId) {
        return topicStates.get(topicId);
    }

    @Scheduled(fixedDelayString = "${CANVAS_FLUSH_INTERVAL_MS:5000}")
    public void flushDirtyStatesToDb() {
        if (dirtyTopics.isEmpty()) {
            return;
        }
        
        logger.debug("Flushing {} dirty topics to DB", dirtyTopics.size());
        
        for (String topicId : dirtyTopics) {
            ReentrantLock lock = topicLocks.get(topicId);
            if (lock == null) continue;
            
            // Try to acquire lock. If we can't, skip and try next time.
            if (lock.tryLock()) {
                try {
                    Map<String, Object> state = topicStates.get(topicId);
                    if (state != null) {
                        topicRepository.findById(topicId).ifPresent(topic -> {
                            topic.setCanvasState(state);
                            topic.setUpdatedAt(LocalDateTime.now());
                            topicRepository.save(topic);
                        });
                    }
                    dirtyTopics.remove(topicId);
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
