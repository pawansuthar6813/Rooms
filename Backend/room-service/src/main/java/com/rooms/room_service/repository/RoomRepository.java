package com.rooms.room_service.repository;

import com.rooms.room_service.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findByIdAndDeletedAtIsNull(UUID id);
}
