package com.newsportal.domain.article.dto;

import com.newsportal.domain.article.ArticleStatus;
import com.newsportal.domain.category.dto.CategoryResponse;
import com.newsportal.domain.tag.dto.TagResponse;
import com.newsportal.domain.user.dto.UserResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class ArticleResponse {
    private UUID id;
    private String title;
    private String titleBn;
    private String slug;
    private String summary;
    private String summaryBn;
    private String content;
    private String contentBn;
    private ArticleStatus status;
    private UserResponse author;
    private CategoryResponse category;
    private Set<TagResponse> tags;
    private String featuredImage;
    private String featuredImageCaption;
    private boolean breaking;
    private boolean featured;
    private Long viewCount;
    private String metaTitle;
    private String metaDescription;
    private Instant publishedAt;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant updatedAt;
}
