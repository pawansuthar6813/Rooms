package com.rooms.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private ResponseEntity<Map<String, Object>> generateFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", serviceName + " Service is currently unavailable. Please try again later.");
        response.put("data", null);
        
        // Return 503 Service Unavailable so that the frontend knows it's a temporary issue
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        return generateFallbackResponse("Auth");
    }

    @RequestMapping("/room")
    public ResponseEntity<Map<String, Object>> roomServiceFallback() {
        return generateFallbackResponse("Room");
    }

    @RequestMapping("/board")
    public ResponseEntity<Map<String, Object>> boardServiceFallback() {
        return generateFallbackResponse("Board");
    }

    @RequestMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatServiceFallback() {
        return generateFallbackResponse("Chat");
    }

    @RequestMapping("/file")
    public ResponseEntity<Map<String, Object>> fileServiceFallback() {
        return generateFallbackResponse("File");
    }
}
