package com.rooms.room_service.repository;

import com.rooms.room_service.entity.RoomInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomInviteRepository extends JpaRepository<RoomInvite, UUID> {
    Optional<RoomInvite> findByToken(UUID token);
    List<RoomInvite> findByInvitedEmailIgnoreCaseAndAcceptedAtIsNull(String email);
}
