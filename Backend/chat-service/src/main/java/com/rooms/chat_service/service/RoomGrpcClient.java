package com.rooms.chat_service.service;

import com.rooms.grpc.room.MemberInfo;
import com.rooms.grpc.room.MemberList;
import com.rooms.grpc.room.RoomIdRequest;
import com.rooms.grpc.room.RoomServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoomGrpcClient {

    @GrpcClient("room-service")
    private RoomServiceGrpc.RoomServiceBlockingStub roomServiceStub;

    public boolean isUserMemberOfRoom(UUID roomId, UUID userId) {
        RoomIdRequest request = RoomIdRequest.newBuilder()
                .setRoomId(roomId.toString())
                .build();
        
        try {
            MemberList memberList = roomServiceStub.getRoomMembers(request);
            List<MemberInfo> members = memberList.getMembersList();
            
            return members.stream()
                    .anyMatch(member -> member.getUserId().equals(userId.toString()));
        } catch (Exception e) {
            // Log error
            return false;
        }
    }
}
