package com.newsportal.domain.media;

import com.newsportal.common.exception.BusinessException;
import com.newsportal.domain.media.dto.MediaResponse;
import com.newsportal.domain.user.User;
import com.newsportal.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml", "application/pdf"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final MinioStorageService storageService;
    private final MediaRepository mediaRepository;

    @Transactional
    public MediaResponse upload(MultipartFile file) {
        validateFile(file);
        User uploader = getCurrentUser();

        String url = storageService.upload(file, "images/articles");

        Media media = Media.builder()
                .filename(file.getOriginalFilename())
                .storedName(url.substring(url.lastIndexOf('/') + 1))
                .url(url)
                .mimeType(file.getContentType())
                .sizeBytes(file.getSize())
                .uploadedBy(uploader)
                .build();

        media = mediaRepository.save(media);
        log.info("Media uploaded: {} by {}", media.getFilename(), uploader.getUsername());
        return toResponse(media);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty", "EMPTY_FILE");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size exceeds 10MB limit", "FILE_TOO_LARGE");
        }
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new BusinessException("File type not allowed: " + file.getContentType(), "INVALID_FILE_TYPE");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    public MediaResponse toResponse(Media m) {
        return MediaResponse.builder()
                .id(m.getId())
                .filename(m.getFilename())
                .url(m.getUrl())
                .thumbnailUrl(m.getThumbnailUrl())
                .mimeType(m.getMimeType())
                .sizeBytes(m.getSizeBytes())
                .altText(m.getAltText())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
