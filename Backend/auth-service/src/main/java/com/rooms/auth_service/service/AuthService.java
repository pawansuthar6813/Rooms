package com.rooms.auth_service.service;

import com.rooms.auth_service.security.JwtService;
import com.rooms.auth_service.dto.TokenResponse;
import com.rooms.auth_service.dto.UserInfoResponse;
import com.rooms.auth_service.entity.User;
import com.rooms.auth_service.exception.UnauthorizedException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final String googleClientId;

    public AuthService(UserService userService, JwtService jwtService,
                       @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.googleClientId = googleClientId;
    }

    public AuthResult signupEmail(String email, String password) {
        User user = userService.createUserWithEmail(email, password);
        TokenResponse tokens = jwtService.createTokens(user);
        UserInfoResponse userInfo = new UserInfoResponse(user.getId(), user.getEmail(), user.getUsername());
        return new AuthResult(tokens, userInfo);
    }

    public AuthResult setUsername(UUID userId, String username) {
        User user = userService.setUsername(userId, username);
        TokenResponse tokens = jwtService.createTokens(user);
        UserInfoResponse userInfo = new UserInfoResponse(user.getId(), user.getEmail(), user.getUsername());
        return new AuthResult(tokens, userInfo);
    }

    public AuthResult login(String emailOrUsername, String password) {
        User user = userService.authenticate(emailOrUsername, password);
        TokenResponse tokens = jwtService.createTokens(user);
        UserInfoResponse userInfo = new UserInfoResponse(user.getId(), user.getEmail(), user.getUsername());
        return new AuthResult(tokens, userInfo);
    }

    public TokenResponse refresh(String refreshToken) {
        return jwtService.refreshTokens(refreshToken);
    }

    public void logout(String refreshToken) {
        jwtService.revokeToken(refreshToken);
    }

    public UserInfoResponse getMe(UUID userId) {
        User user = userService.getUserById(userId);
        return new UserInfoResponse(user.getId(), user.getEmail(), user.getUsername());
    }

    public void updatePassword(UUID userId, String oldPassword, String newPassword) {
        userService.updatePassword(userId, oldPassword, newPassword);
    }

    public AuthResult loginWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                User user = userService.handleOAuthLogin(payload.getEmail(), payload.getSubject());
                TokenResponse tokens = jwtService.createTokens(user);
                UserInfoResponse userInfo = new UserInfoResponse(user.getId(), user.getEmail(), user.getUsername());
                return new AuthResult(tokens, userInfo);
            } else {
                throw new UnauthorizedException("Invalid Google ID token");
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("OAuth login failed: " + e.getMessage());
        }
    }

    public record AuthResult(TokenResponse tokens, UserInfoResponse userInfo) {}
}
