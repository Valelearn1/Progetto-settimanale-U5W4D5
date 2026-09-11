package com.example.demo.dto.response;

import com.example.demo.entity.Photo;

import java.time.Instant;
import java.util.UUID;

public record PhotoResponse(
        UUID id,
        String url,
        String contentType,
        long sizeBytes,
        int position,
        Instant createdAt
) {
    public static PhotoResponse from(Photo photo) {
        return new PhotoResponse(
                photo.getId(),
                "/files/" + photo.getFilePath(),
                photo.getContentType(),
                photo.getSizeBytes(),
                photo.getPosition(),
                photo.getCreatedAt()
        );
    }
}
