package com.example.demo.dto.response;

import com.example.demo.entity.Post;
import com.example.demo.entity.enums.CaptureMode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String text,
        LocationResponse location,
        CaptureMode captureMode,
        Instant createdAt,
        Instant updatedAt,
        UUID userId,
        String username,
        List<PhotoResponse> photos,
        List<DocumentResponse> documents
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getText(),
                LocationResponse.from(post.getLocation()),
                post.getCaptureMode(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getPhotos().stream().map(PhotoResponse::from).toList(),
                post.getDocuments().stream().map(DocumentResponse::from).toList()
        );
    }
}
