package com.newsportal.domain.media;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    Page<Media> findByUploadedByIdOrderByCreatedAtDesc(UUID uploadedById, Pageable pageable);
}
