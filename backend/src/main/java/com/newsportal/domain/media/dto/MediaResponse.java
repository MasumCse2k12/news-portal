package com.newsportal.domain.media.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class MediaResponse {
    private UUID id;
    private String filename;
    private String url;
    private String thumbnailUrl;
    private String mimeType;
    private Long sizeBytes;
    private String altText;
    private Instant createdAt;
}
