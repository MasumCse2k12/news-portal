package com.newsportal.domain.article;

import com.newsportal.common.ApiResponse;
import com.newsportal.common.PageResponse;
import com.newsportal.domain.article.dto.ArticleCreateRequest;
import com.newsportal.domain.article.dto.ArticleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Articles")
public class ArticleController {

    private final ArticleService articleService;

    // ---- Public endpoints ----

    @GetMapping("/api/v1/public/articles")
    @Operation(summary = "Get published articles (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(articleService.getPublished(pageable)));
    }

    @GetMapping("/api/v1/public/articles/breaking")
    @Operation(summary = "Get breaking news")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getBreaking() {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getBreaking()));
    }

    @GetMapping("/api/v1/public/articles/featured")
    @Operation(summary = "Get featured articles")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getFeatured()));
    }

    @GetMapping("/api/v1/public/articles/{slug}")
    @Operation(summary = "Get article by slug")
    public ResponseEntity<ApiResponse<ArticleResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getBySlug(slug)));
    }

    @GetMapping("/api/v1/public/categories/{slug}/articles")
    @Operation(summary = "Get articles by category")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(articleService.getByCategory(slug, pageable)));
    }

    @GetMapping("/api/v1/public/tags/{slug}/articles")
    @Operation(summary = "Get articles by tag")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getByTag(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(articleService.getByTag(slug, pageable)));
    }

    // ---- CMS endpoints ----

    @GetMapping("/api/v1/cms/articles")
    @PreAuthorize("hasAnyRole('JOURNALIST','EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my articles")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponse>>> getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(articleService.getMyArticles(pageable)));
    }

    @PostMapping("/api/v1/cms/articles")
    @PreAuthorize("hasAnyRole('JOURNALIST','EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new article (saved as DRAFT)")
    public ResponseEntity<ApiResponse<ArticleResponse>> create(@Valid @RequestBody ArticleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Article created", articleService.create(request)));
    }

    @PatchMapping("/api/v1/cms/articles/{id}/submit")
    @PreAuthorize("hasAnyRole('JOURNALIST','EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit article for editorial review")
    public ResponseEntity<ApiResponse<ArticleResponse>> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Submitted for review", articleService.submitForReview(id)));
    }

    @PatchMapping("/api/v1/cms/articles/{id}/publish")
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publish article immediately")
    public ResponseEntity<ApiResponse<ArticleResponse>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Article published", articleService.publish(id)));
    }

    @PatchMapping("/api/v1/cms/articles/{id}/schedule")
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Schedule article for future publish")
    public ResponseEntity<ApiResponse<ArticleResponse>> schedule(
            @PathVariable UUID id,
            @RequestParam Instant scheduledAt) {
        return ResponseEntity.ok(ApiResponse.ok("Article scheduled", articleService.schedule(id, scheduledAt)));
    }

    @PatchMapping("/api/v1/cms/articles/{id}/reject")
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject article with note")
    public ResponseEntity<ApiResponse<ArticleResponse>> reject(
            @PathVariable UUID id,
            @RequestParam String note) {
        return ResponseEntity.ok(ApiResponse.ok("Article rejected", articleService.reject(id, note)));
    }

    @PatchMapping("/api/v1/cms/articles/{id}/archive")
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Archive article")
    public ResponseEntity<ApiResponse<ArticleResponse>> archive(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Article archived", articleService.archive(id)));
    }

    @PatchMapping("/api/v1/cms/articles/{id}/breaking")
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle breaking news flag")
    public ResponseEntity<ApiResponse<ArticleResponse>> toggleBreaking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(articleService.toggleBreaking(id)));
    }
}
