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
        // Valorizzato solo se il documento e' allegato a un post:
        // serve al frontend per mostrare l'etichetta "allegato a un post".
        UUID postId,
        Instant createdAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                "/files/" + document.getFilePath(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getExtractedText(),
                document.getPost() == null ? null : document.getPost().getId(),
                document.getCreatedAt()
        );
    }
}
