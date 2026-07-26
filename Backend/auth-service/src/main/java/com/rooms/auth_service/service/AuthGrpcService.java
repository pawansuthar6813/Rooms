package com.rooms.auth_service.service;

import com.rooms.auth_service.entity.User;
import com.rooms.auth_service.grpc.AuthServiceGrpc;
import com.rooms.auth_service.grpc.GetUserRequest;
import com.rooms.auth_service.grpc.GetUserResponse;
import com.rooms.auth_service.grpc.ValidateTokenRequest;
import com.rooms.auth_service.grpc.ValidateTokenResponse;
import com.rooms.auth_service.security.JwtService;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final String accessSecret;
    private final UserService userService;

    public AuthGrpcService(@Value("${jwt.access.secret}") String accessSecret, UserService userService) {
        this.accessSecret = accessSecret;
        this.userService = userService;
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        String token = request.getAccessToken();
        try {
            SecretKey key = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setIsValid(true)
                    .setUserId(userId != null ? userId : "")
                    .setUsername(username != null ? username : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (JwtException | IllegalArgumentException e) {
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setIsValid(false)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserById(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            User user = userService.getUserById(userId);
            
            GetUserResponse response = GetUserResponse.newBuilder()
                    .setUserId(user.getId().toString())
                    .setUsername(user.getUsername() != null ? user.getUsername() : "")
                    .setEmail(user.getEmail() != null ? user.getEmail() : "")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("User not found or invalid ID")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
