package com.rooms.room_service.service;

import com.rooms.grpc.room.MemberInfo;
import com.rooms.grpc.room.MemberList;
import com.rooms.grpc.room.RoomIdRequest;
import com.rooms.grpc.room.RoomServiceGrpc;
import com.rooms.room_service.entity.RoomMember;
import com.rooms.room_service.repository.RoomMemberRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class RoomGrpcServiceImpl extends RoomServiceGrpc.RoomServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(RoomGrpcServiceImpl.class);
    private final RoomMemberRepository roomMemberRepository;

    public RoomGrpcServiceImpl(RoomMemberRepository roomMemberRepository) {
        this.roomMemberRepository = roomMemberRepository;
    }

    @Override
    public void getRoomMembers(RoomIdRequest request, StreamObserver<MemberList> responseObserver) {
        try {
            UUID roomId = UUID.fromString(request.getRoomId());
            List<RoomMember> members = roomMemberRepository.findByRoomId(roomId);

            List<MemberInfo> memberInfos = members.stream()
                    .map(m -> MemberInfo.newBuilder()
                            .setUserId(m.getUserId().toString())
                            .setRole(m.getRole().name())
                            .build())
                    .collect(Collectors.toList());

            MemberList memberList = MemberList.newBuilder()
                    .addAllMembers(memberInfos)
                    .build();

            responseObserver.onNext(memberList);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID format for room ID: {}", request.getRoomId());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid Room ID format")
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Error getting room members", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
}
