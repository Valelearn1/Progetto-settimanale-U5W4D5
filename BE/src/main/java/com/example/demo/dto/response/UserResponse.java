package com.example.demo.dto.response;

import com.example.demo.entity.User;

import java.time.Instant;
import java.util.UUID;

// Non contiene la password, nemmeno l'hash: non deve mai uscire dal
// backend.
public record UserResponse(
        UUID id,
        String nomeCompleto,
        String username,
        String email,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNomeCompleto(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
