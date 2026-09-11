package com.example.demo.dto.response;

import com.example.demo.entity.Document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String url,
        String contentType,
        long sizeBytes,
        String extractedText,
        Instant createdAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                "/files/" + document.getFilePath(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getExtractedText(),
                document.getCreatedAt()
        );
    }
}
