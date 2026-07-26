package com.rooms.room_service.service;

import com.rooms.grpc.auth.AuthServiceGrpc;
import com.rooms.grpc.auth.UserIdRequest;
import com.rooms.grpc.auth.UserInfo;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthGrpcClient {

    @GrpcClient("auth-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    public UserInfo getUserById(UUID userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();
        return authServiceStub.getUserById(request);
    }
}
