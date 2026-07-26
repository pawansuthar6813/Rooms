package com.rooms.auth_service.service;

import com.rooms.auth_service.entity.User;
import com.rooms.grpc.auth.AuthServiceGrpc;
import com.rooms.grpc.auth.UserIdRequest;
import com.rooms.grpc.auth.UserInfo;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class AuthGrpcServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AuthGrpcServiceImpl.class);
    private final UserService userService;

    public AuthGrpcServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUserById(UserIdRequest request, StreamObserver<UserInfo> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            User user = userService.getUserById(userId);

            UserInfo userInfo = UserInfo.newBuilder()
                    .setUserId(user.getId().toString())
                    .setUsername(user.getUsername() != null ? user.getUsername() : "")
                    .setEmail(user.getEmail() != null ? user.getEmail() : "")
                    .build();

            responseObserver.onNext(userInfo);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID format: {}", request.getUserId());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid User ID format")
                    .asRuntimeException());
        } catch (Exception e) {
            if (e.getMessage().contains("User not found")) {
                logger.warn("User not found: {}", request.getUserId());
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("User not found")
                        .asRuntimeException());
            } else {
                logger.error("Error getting user by ID", e);
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Internal server error")
                        .asRuntimeException());
            }
        }
    }
}
