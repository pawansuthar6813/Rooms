package com.rooms.auth_service.util;

import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private CookieUtil() {}

    public static String createCookie(String name, String value, long maxAgeMs, boolean isProd) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(isProd) // true in prod, false in dev
                .path("/")
                .maxAge(maxAgeMs / 1000)
                .sameSite(isProd ? "Strict" : "Lax") // Strict in prod, Lax in dev for cross-port requests
                .build()
                .toString();
    }
    
    public static String clearCookie(String name, boolean isProd) {
        return createCookie(name, "", 0, isProd);
    }
}
