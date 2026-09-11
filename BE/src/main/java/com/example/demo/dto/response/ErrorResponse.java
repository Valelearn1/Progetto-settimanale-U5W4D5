package com.example.demo.dto.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        List<String> details
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message, List.of());
    }

    public static ErrorResponse of(int status, String message, List<String> details) {
        return new ErrorResponse(Instant.now(), status, message, details);
    }
}
