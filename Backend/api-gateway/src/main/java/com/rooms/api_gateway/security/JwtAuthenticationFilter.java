package com.rooms.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip auth routes and public routes
        if (path.startsWith("/api/v1/auth/signup/email") || path.startsWith("/api/v1/auth/login") || 
            path.startsWith("/api/v1/auth/oauth") || path.startsWith("/api/v1/auth/refresh")) {
            return chain.filter(exchange);
        }

        List<HttpCookie> cookies = request.getCookies().get("access_token");
        if (cookies == null || cookies.isEmpty()) {
            logger.warn("No access_token cookie found for path: {}", path);
            return onError(exchange, "No access_token cookie found", HttpStatus.UNAUTHORIZED);
        }

        String token = cookies.get(0).getValue();
        try {
            Claims claims = jwtService.validateAndGetClaims(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            if (username == null && !path.startsWith("/api/v1/auth/")) {
                logger.warn("User {} has not set a username. Blocking access to {}", userId, path);
                return onError(exchange, "Username setup required", HttpStatus.FORBIDDEN);
            }

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username != null ? username : "")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (JwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
