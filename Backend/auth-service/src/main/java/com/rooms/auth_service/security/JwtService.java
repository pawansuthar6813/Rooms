package com.rooms.auth_service.security;

import com.rooms.auth_service.dto.TokenResponse;
import com.rooms.auth_service.entity.RefreshToken;
import com.rooms.auth_service.entity.User;
import com.rooms.auth_service.exception.UnauthorizedException;
import com.rooms.auth_service.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final String accessSecret;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;

    public JwtService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.access.secret}") String accessSecret,
            @Value("${jwt.access.expiration-ms}") long accessExpiryMs,
            @Value("${jwt.refresh.expiration-ms}") long refreshExpiryMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessSecret = accessSecret;
        this.accessExpiryMs = accessExpiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
    }

    @Transactional
    public TokenResponse createTokens(User user) {
        String family = UUID.randomUUID().toString();
        return generateTokens(user, family);
    }

    private TokenResponse generateTokens(User user, String family) {
        // Generate Access Token
        SecretKey key = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        String accessToken = Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiryMs))
                .signWith(key)
                .compact();

        // Generate Refresh Token
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String refreshTokenStr = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenHash = hashToken(refreshTokenStr);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .family(family)
                .expiresAt(Instant.now().plusMillis(refreshExpiryMs))
                .revoked(false)
                .build();
        
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, refreshTokenStr);
    }

    @Transactional
    public TokenResponse refreshTokens(String refreshTokenStr) {
        String tokenHash = hashToken(refreshTokenStr);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            // Reuse detected! Revoke the whole family.
            refreshTokenRepository.revokeFamily(storedToken.getFamily());
            throw new UnauthorizedException("Refresh token reuse detected. Family revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        // Revoke the old token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Generate new tokens in the same family
        return generateTokens(storedToken.getUser(), storedToken.getFamily());
    }

    @Transactional
    public void revokeToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) return;
        String tokenHash = hashToken(refreshTokenStr);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            refreshTokenRepository.revokeFamily(token.getFamily());
        });
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
