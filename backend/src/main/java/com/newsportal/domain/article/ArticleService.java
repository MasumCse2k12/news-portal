package com.newsportal.domain.article;

import com.newsportal.common.PageResponse;
import com.newsportal.common.exception.BusinessException;
import com.newsportal.common.exception.ResourceNotFoundException;
import com.newsportal.domain.article.dto.ArticleCreateRequest;
import com.newsportal.domain.article.dto.ArticleResponse;
import com.newsportal.domain.category.Category;
import com.newsportal.domain.category.CategoryRepository;
import com.newsportal.domain.tag.Tag;
import com.newsportal.domain.tag.TagRepository;
import com.newsportal.domain.user.User;
import com.newsportal.domain.user.UserService;
import com.newsportal.messaging.publisher.ArticleEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final ArticleEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(value = {"articles", "breaking", "trending"}, allEntries = true)
    public ArticleResponse create(ArticleCreateRequest request) {
        User author = getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Set<Tag> tags = resolveTags(request.getTagSlugs());

        String slug = generateSlug(request.getTitle());
        if (articleRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Article article = Article.builder()
                .title(request.getTitle())
                .titleBn(request.getTitleBn())
                .slug(slug)
                .summary(request.getSummary())
                .summaryBn(request.getSummaryBn())
                .content(request.getContent())
                .contentBn(request.getContentBn())
                .author(author)
                .category(category)
                .tags(tags)
                .featuredImage(request.getFeaturedImage())
                .featuredImageCaption(request.getFeaturedImageCaption())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .status(ArticleStatus.DRAFT)
                .build();

        article = articleRepository.save(article);
        log.info("Article created: {} by {}", article.getSlug(), author.getUsername());
        return toResponse(article);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "articles", key = "#slug")
    public ArticleResponse getBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            // Only staff can see non-published articles
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                throw new ResourceNotFoundException("Article", "slug", slug);
            }
        }

        // Publish view event asynchronously
        eventPublisher.publishViewedEvent(article.getId());

        return toResponse(article);
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getPublished(Pageable pageable) {
        Page<Article> page = articleRepository.findByStatusOrderByPublishedAtDesc(
                ArticleStatus.PUBLISHED, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getByCategory(String categorySlug, Pageable pageable) {
        Page<Article> page = articleRepository.findByCategorySlugAndStatusOrderByPublishedAtDesc(
                categorySlug, ArticleStatus.PUBLISHED, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getByTag(String tagSlug, Pageable pageable) {
        Page<Article> page = articleRepository.findByTagsSlugAndStatusOrderByPublishedAtDesc(
                tagSlug, ArticleStatus.PUBLISHED, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    @Cacheable("breaking")
    public List<ArticleResponse> getBreaking() {
        return articleRepository.findByBreakingTrueAndStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED)
                .stream().limit(5).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getFeatured() {
        return articleRepository.findByFeaturedTrueAndStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED)
                .stream().limit(10).map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = {"articles", "breaking", "trending"}, allEntries = true)
    public ArticleResponse submitForReview(UUID id) {
        Article article = findOwnedArticleOrThrow(id);
        if (article.getStatus() != ArticleStatus.DRAFT) {
            throw new BusinessException("Only DRAFT articles can be submitted", "INVALID_STATUS_TRANSITION");
        }
        article.setStatus(ArticleStatus.REVIEW);
        return toResponse(articleRepository.save(article));
    }

    @Transactional
    @CacheEvict(value = {"articles", "breaking", "trending"}, allEntries = true)
    public ArticleResponse publish(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        if (article.getStatus() != ArticleStatus.REVIEW && article.getStatus() != ArticleStatus.DRAFT) {
            throw new BusinessException("Article cannot be published from status: " + article.getStatus(),
                    "INVALID_STATUS_TRANSITION");
        }

        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishedAt(Instant.now());
        article = articleRepository.save(article);

        eventPublisher.publishArticlePublishedEvent(article);
        log.info("Article published: {}", article.getSlug());
        return toResponse(article);
    }

    @Transactional
    @CacheEvict(value = {"articles", "breaking", "trending"}, allEntries = true)
    public ArticleResponse schedule(UUID id, Instant scheduledAt) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        if (scheduledAt.isBefore(Instant.now())) {
            throw new BusinessException("Scheduled time must be in the future", "INVALID_SCHEDULE_TIME");
        }

        article.setStatus(ArticleStatus.SCHEDULED);
        article.setScheduledAt(scheduledAt);
        return toResponse(articleRepository.save(article));
    }

    @Transactional
    @CacheEvict(value = {"articles", "breaking", "trending"}, allEntries = true)
    public ArticleResponse reject(UUID id, String note) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        article.setStatus(ArticleStatus.DRAFT);
        article.setRejectionNote(note);
        return toResponse(articleRepository.save(article));
    }

    @Transactional
    @CacheEvict(value = {"articles", "breaking", "trending"}, allEntries = true)
    public ArticleResponse archive(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        article.setStatus(ArticleStatus.ARCHIVED);
        return toResponse(articleRepository.save(article));
    }

    @Transactional
    @CacheEvict(value = {"articles", "breaking"}, allEntries = true)
    public ArticleResponse toggleBreaking(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        article.setBreaking(!article.isBreaking());
        return toResponse(articleRepository.save(article));
    }

    // ---- CMS: journalist's own articles ----

    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getMyArticles(Pageable pageable) {
        User author = getCurrentUser();
        Page<Article> page = articleRepository.findByAuthorIdAndStatusInOrderByCreatedAtDesc(
                author.getId(),
                List.of(ArticleStatus.values()),
                pageable
        );
        return PageResponse.from(page.map(this::toResponse));
    }

    // ---- Helpers ----

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    private Article findOwnedArticleOrThrow(UUID id) {
        User currentUser = getCurrentUser();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You don't own this article", "ACCESS_DENIED");
        }
        return article;
    }

    private Set<Tag> resolveTags(Set<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return new HashSet<>();
        return new HashSet<>(tagRepository.findBySlugIn(slugs));
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    public ArticleResponse toResponse(Article a) {
        return ArticleResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .titleBn(a.getTitleBn())
                .slug(a.getSlug())
                .summary(a.getSummary())
                .summaryBn(a.getSummaryBn())
                .content(a.getContent())
                .contentBn(a.getContentBn())
                .status(a.getStatus())
                .author(userService.toResponse(a.getAuthor()))
                .category(a.getCategory() != null ? categoryToResponse(a.getCategory()) : null)
                .tags(a.getTags() != null ? a.getTags().stream().map(this::tagToResponse).collect(java.util.stream.Collectors.toSet()) : null)
                .featuredImage(a.getFeaturedImage())
                .featuredImageCaption(a.getFeaturedImageCaption())
                .breaking(a.isBreaking())
                .featured(a.isFeatured())
                .viewCount(a.getViewCount())
                .metaTitle(a.getMetaTitle())
                .metaDescription(a.getMetaDescription())
                .publishedAt(a.getPublishedAt())
                .scheduledAt(a.getScheduledAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private com.newsportal.domain.category.dto.CategoryResponse categoryToResponse(Category c) {
        return com.newsportal.domain.category.dto.CategoryResponse.builder()
                .id(c.getId()).name(c.getName()).nameBn(c.getNameBn()).slug(c.getSlug()).build();
    }

    private com.newsportal.domain.tag.dto.TagResponse tagToResponse(Tag t) {
        return com.newsportal.domain.tag.dto.TagResponse.builder()
                .id(t.getId()).name(t.getName()).nameBn(t.getNameBn()).slug(t.getSlug()).build();
    }
}
