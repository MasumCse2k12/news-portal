package com.newsportal.domain.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Getter
public class ArticleCreateRequest {

    @NotBlank
    @Size(max = 500)
    private String title;

    @Size(max = 500)
    private String titleBn;

    @Size(max = 1000)
    private String summary;

    private String summaryBn;

    @NotBlank
    private String content;

    private String contentBn;

    @NotNull
    private Integer categoryId;

    private Set<String> tagSlugs;

    private String featuredImage;
    private String featuredImageCaption;

    private String metaTitle;
    private String metaDescription;

    private Instant scheduledAt;
}
