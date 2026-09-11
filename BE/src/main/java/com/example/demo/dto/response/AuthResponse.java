package com.example.demo.dto.response;

public record AuthResponse(
        String token,
        // Durata del token in millisecondi: serve al frontend per
        // sapere quando dovra' rifare il login.
        long expiresIn,
        UserResponse user
) {
}
