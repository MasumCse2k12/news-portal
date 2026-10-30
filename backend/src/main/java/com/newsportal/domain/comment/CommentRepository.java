package com.newsportal.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByArticleIdAndStatusAndParentIsNullOrderByCreatedAtAsc(
            UUID articleId, CommentStatus status, Pageable pageable);

    Page<Comment> findByStatusOrderByCreatedAtDesc(CommentStatus status, Pageable pageable);
}
