package com.rooms.room_service.service;

import com.rooms.grpc.auth.UserInfo;
import com.rooms.room_service.dto.CreateRoomRequest;
import com.rooms.room_service.dto.RoomDto;
import com.rooms.room_service.dto.RoomMemberDto;
import com.rooms.room_service.entity.MemberRole;
import com.rooms.room_service.entity.Room;
import com.rooms.room_service.entity.RoomInvite;
import com.rooms.room_service.entity.RoomMember;
import com.rooms.room_service.exception.BadRequestException;
import com.rooms.room_service.exception.ResourceNotFoundException;
import com.rooms.room_service.exception.UnauthorizedException;
import com.rooms.room_service.repository.RoomInviteRepository;
import com.rooms.room_service.repository.RoomMemberRepository;
import com.rooms.room_service.repository.RoomRepository;
import io.grpc.StatusRuntimeException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomInviteRepository roomInviteRepository;
    private final AuthGrpcClient authGrpcClient;
    private final JavaMailSender mailSender;

    public RoomService(RoomRepository roomRepository, RoomMemberRepository roomMemberRepository, RoomInviteRepository roomInviteRepository, AuthGrpcClient authGrpcClient, JavaMailSender mailSender) {
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.roomInviteRepository = roomInviteRepository;
        this.authGrpcClient = authGrpcClient;
        this.mailSender = mailSender;
    }

    @Transactional
    @CacheEvict(value = "userRooms", key = "#ownerId")
    public RoomDto createRoom(CreateRoomRequest request, UUID ownerId) {
        Room room = Room.builder()
                .name(request.name())
                .description(request.description())
                .ownerId(ownerId)
                .build();
        
        room = roomRepository.save(room);

        RoomMember ownerMember = RoomMember.builder()
                .room(room)
                .userId(ownerId)
                .role(MemberRole.OWNER)
                .build();
        roomMemberRepository.save(ownerMember);

        room.getMembers().add(ownerMember);
        return mapToDto(room);
    }

    @Cacheable(value = "userRooms", key = "#userId")
    public List<RoomDto> getUserRooms(UUID userId) {
        List<RoomMember> memberships = roomMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(RoomMember::getRoom)
                .filter(room -> room.getDeletedAt() == null)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public RoomDto getRoomDetails(UUID roomId, UUID userId) {
        Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new UnauthorizedException("Access denied");
        }

        return mapToDto(room);
    }

    @Transactional
    @CacheEvict(value = "userRooms", allEntries = true)
    public void deleteRoom(UUID roomId, UUID requesterId) {
        Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getOwnerId().equals(requesterId)) {
            throw new UnauthorizedException("Only owner can delete room");
        }

        room.setDeletedAt(LocalDateTime.now());
        roomRepository.save(room);
    }

    @Transactional
    public void generateAndSendInvite(UUID roomId, String email, UUID userId) {
        Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        RoomMember member = roomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new UnauthorizedException("Access denied"));

        if (member.getRole() == MemberRole.VIEWER) {
            throw new UnauthorizedException("Viewers cannot invite");
        }

        RoomInvite invite = RoomInvite.builder()
                .room(room)
                .invitedEmail(email)
                .token(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        roomInviteRepository.save(invite);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Invitation to join " + room.getName());
        message.setText("You have been invited to join the room. Your invite token is: " + invite.getToken());
        try {
            mailSender.send(message);
        } catch (org.springframework.mail.MailException e) {
            // Log the error but don't fail the transaction
            org.slf4j.LoggerFactory.getLogger(RoomService.class)
                    .warn("Failed to send invite email to {}. Is the mail server running?", email, e);
        }
    }

    @Transactional
    @CacheEvict(value = "userRooms", key = "#userId")
    public void acceptInvite(UUID token, UUID userId) {
        RoomInvite invite = roomInviteRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite"));

        if (invite.getAcceptedAt() != null || invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invite expired or already accepted");
        }

        if (roomMemberRepository.existsByRoomIdAndUserId(invite.getRoom().getId(), userId)) {
            throw new BadRequestException("Already a member");
        }

        RoomMember newMember = RoomMember.builder()
                .room(invite.getRoom())
                .userId(userId)
                .role(MemberRole.EDITOR)
                .build();

        roomMemberRepository.save(newMember);

        invite.setAcceptedAt(LocalDateTime.now());
        roomInviteRepository.save(invite);
    }

    @Transactional
    @CacheEvict(value = "userRooms", allEntries = true)
    public void removeMember(UUID roomId, UUID memberIdToRemove, UUID requesterId) {
        Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getOwnerId().equals(requesterId) && !memberIdToRemove.equals(requesterId)) {
            throw new UnauthorizedException("Only owner can remove others");
        }
        
        if (room.getOwnerId().equals(memberIdToRemove)) {
            throw new BadRequestException("Owner cannot be removed");
        }

        RoomMember member = roomMemberRepository.findByRoomIdAndUserId(roomId, memberIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        roomMemberRepository.delete(member);
    }

    @Transactional
    @CacheEvict(value = "userRooms", allEntries = true)
    public void changeRole(UUID roomId, UUID memberId, MemberRole newRole, UUID requesterId) {
        Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getOwnerId().equals(requesterId)) {
            throw new UnauthorizedException("Only owner can change roles");
        }
        
        if (room.getOwnerId().equals(memberId)) {
            throw new BadRequestException("Cannot change owner's role");
        }

        RoomMember member = roomMemberRepository.findByRoomIdAndUserId(roomId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        member.setRole(newRole);
        roomMemberRepository.save(member);
    }

    public List<com.rooms.room_service.dto.PendingInviteDto> getPendingInvites(UUID userId) {
        String email = "";
        try {
            UserInfo userInfo = authGrpcClient.getUserById(userId);
            email = userInfo.getEmail();
        } catch (StatusRuntimeException e) {
            throw new UnauthorizedException("User email not found");
        }

        List<RoomInvite> invites = roomInviteRepository.findByInvitedEmailIgnoreCaseAndAcceptedAtIsNull(email);
        return invites.stream()
                .filter(invite -> invite.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(invite -> new com.rooms.room_service.dto.PendingInviteDto(
                        invite.getToken(),
                        invite.getRoom().getName(),
                        "Unknown",
                        invite.getExpiresAt()
                ))
                .collect(Collectors.toList());
    }

    private RoomDto mapToDto(Room room) {
        List<RoomMemberDto> memberDtos = room.getMembers().stream().map(m -> {
            String username = "";
            String email = "";
            try {
                UserInfo userInfo = authGrpcClient.getUserById(m.getUserId());
                username = userInfo.getUsername();
                email = userInfo.getEmail();
            } catch (StatusRuntimeException e) {
                // Ignore missing user in auth service for now
            }
            return new RoomMemberDto(
                m.getUserId(),
                username,
                email,
                m.getRole(),
                m.getJoinedAt()
            );
        }).collect(Collectors.toList());

        return new RoomDto(
            room.getId(),
            room.getName(),
            room.getDescription(),
            room.getOwnerId(),
            room.getCreatedAt(),
            memberDtos
        );
    }
}
