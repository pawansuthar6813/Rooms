package com.rooms.auth_service.controller;

import com.rooms.auth_service.dto.ApiResponse;
import com.rooms.auth_service.dto.LoginRequest;
import com.rooms.auth_service.dto.SignupEmailRequest;
import com.rooms.auth_service.dto.SignupUsernameRequest;
import com.rooms.auth_service.dto.TokenResponse;
import com.rooms.auth_service.dto.UserInfoResponse;
import com.rooms.auth_service.dto.OAuthLoginRequest;
import com.rooms.auth_service.dto.UpdatePasswordRequest;
import com.rooms.auth_service.exception.UnauthorizedException;
import com.rooms.auth_service.service.AuthService;
import com.rooms.auth_service.service.AuthService.AuthResult;
import com.rooms.auth_service.util.CookieUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;
    private final boolean isProd;

    public AuthController(AuthService authService,
                          @Value("${jwt.access.expiration-ms}") long accessExpiryMs,
                          @Value("${jwt.refresh.expiration-ms}") long refreshExpiryMs,
                          @Value("${spring.profiles.active:dev}") String activeProfile) {
        this.authService = authService;
        this.accessExpiryMs = accessExpiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
        this.isProd = "prod".equalsIgnoreCase(activeProfile);
    }

    @PostMapping("/signup/email")
    public ResponseEntity<ApiResponse<UserInfoResponse>> signupEmail(@Valid @RequestBody SignupEmailRequest request) {
        AuthResult result = authService.signupEmail(request.email(), request.password());
        return buildAuthResponse(result, "Signup successful");
    }

    @PostMapping("/signup/username")
    public ResponseEntity<ApiResponse<UserInfoResponse>> setUsername(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody SignupUsernameRequest request) {
        if (userId == null) throw new UnauthorizedException("Unauthorized");
        AuthResult result = authService.setUsername(UUID.fromString(userId), request.username());
        return buildAuthResponse(result, "Username set successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserInfoResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authService.login(request.emailOrUsername(), request.password());
        return buildAuthResponse(result, "Login successful");
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<ApiResponse<UserInfoResponse>> loginWithGoogle(@Valid @RequestBody OAuthLoginRequest request) {
        AuthResult result = authService.loginWithGoogle(request.idToken());
        return buildAuthResponse(result, "Google login successful");
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                                            @Valid @RequestBody UpdatePasswordRequest request) {
        if (userId == null) throw new UnauthorizedException("Unauthorized");
        authService.updatePassword(UUID.fromString(userId), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Password updated successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null) throw new UnauthorizedException("No refresh token");
        TokenResponse tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("access_token", tokens.accessToken(), accessExpiryMs, isProd))
                .header(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("refresh_token", tokens.refreshToken(), refreshExpiryMs, isProd))
                .body(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(value = "refresh_token", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, CookieUtil.clearCookie("access_token", isProd))
                .header(HttpHeaders.SET_COOKIE, CookieUtil.clearCookie("refresh_token", isProd))
                .body(new ApiResponse<>(true, HttpStatus.OK.value(), null, "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMe(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) throw new UnauthorizedException("Unauthorized");
        UserInfoResponse userInfo = authService.getMe(UUID.fromString(userId));
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), userInfo, "User profile retrieved"));
    }

    private ResponseEntity<ApiResponse<UserInfoResponse>> buildAuthResponse(AuthResult result, String message) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("access_token", result.tokens().accessToken(), accessExpiryMs, isProd))
                .header(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("refresh_token", result.tokens().refreshToken(), refreshExpiryMs, isProd))
                .body(new ApiResponse<>(true, HttpStatus.OK.value(), result.userInfo(), message));
    }
}
