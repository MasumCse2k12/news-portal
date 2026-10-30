package com.newsportal.domain.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    Optional<Article> findBySlug(String slug);

    Page<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Page<Article> findByCategorySlugAndStatusOrderByPublishedAtDesc(
            String categorySlug, ArticleStatus status, Pageable pageable);

    Page<Article> findByTagsSlugAndStatusOrderByPublishedAtDesc(
            String tagSlug, ArticleStatus status, Pageable pageable);

    List<Article> findByBreakingTrueAndStatusOrderByPublishedAtDesc(ArticleStatus status);

    List<Article> findByFeaturedTrueAndStatusOrderByPublishedAtDesc(ArticleStatus status);

    List<Article> findTop10ByStatusAndPublishedAtAfterOrderByViewCountDesc(
            ArticleStatus status, Instant since);

    Page<Article> findByAuthorIdAndStatusInOrderByCreatedAtDesc(
            UUID authorId, List<ArticleStatus> statuses, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'SCHEDULED' AND a.scheduledAt <= :now")
    List<Article> findScheduledArticlesDue(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + :count WHERE a.id = :id")
    void incrementViewCount(@Param("id") UUID id, @Param("count") long count);
}
