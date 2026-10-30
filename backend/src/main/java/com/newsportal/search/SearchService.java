package com.newsportal.search;

import com.newsportal.common.PageResponse;
import com.newsportal.domain.article.Article;
import com.newsportal.domain.article.dto.ArticleResponse;
import com.newsportal.domain.category.dto.CategoryResponse;
import com.newsportal.domain.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ArticleSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public void indexArticle(Article article) {
        try {
            searchRepository.save(toDocument(article));
            log.debug("Indexed article: {}", article.getSlug());
        } catch (Exception e) {
            log.error("Failed to index article: {}", article.getSlug(), e);
        }
    }

    public void removeFromIndex(UUID articleId) {
        try {
            searchRepository.deleteById(articleId.toString());
            log.debug("Removed article from index: {}", articleId);
        } catch (Exception e) {
            log.error("Failed to remove article from index: {}", articleId, e);
        }
    }

    public PageResponse<ArticleResponse> search(String keyword, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(mm -> mm
                        .query(keyword)
                        .fields(List.of("title^3", "titleBn^3", "summary^2", "summaryBn^2", "content"))
                        .fuzziness("AUTO")
                ))
                .withPageable(pageable)
                .build();

        SearchHits<ArticleDocument> hits = elasticsearchOperations.search(query, ArticleDocument.class);

        List<ArticleResponse> results = hits.stream()
                .map(SearchHit::getContent)
                .map(this::toResponse)
                .collect(Collectors.toList());

        long total = hits.getTotalHits();
        int totalPages = pageable.getPageSize() == 0 ? 1
                : (int) Math.ceil((double) total / pageable.getPageSize());

        return PageResponse.<ArticleResponse>builder()
                .content(results)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(total)
                .totalPages(totalPages)
                .first(pageable.getPageNumber() == 0)
                .last(pageable.getPageNumber() >= totalPages - 1)
                .build();
    }

    // ---- Mapping helpers ----

    private ArticleDocument toDocument(Article a) {
        return ArticleDocument.builder()
                .id(a.getId().toString())
                .title(a.getTitle())
                .titleBn(a.getTitleBn())
                .summary(a.getSummary())
                .summaryBn(a.getSummaryBn())
                .content(a.getContent())
                .slug(a.getSlug())
                .featuredImage(a.getFeaturedImage())
                .authorId(a.getAuthor() != null ? a.getAuthor().getId().toString() : null)
                .authorFullName(a.getAuthor() != null ? a.getAuthor().getFullName() : null)
                .categoryName(a.getCategory() != null ? a.getCategory().getName() : null)
                .categoryNameBn(a.getCategory() != null ? a.getCategory().getNameBn() : null)
                .categorySlug(a.getCategory() != null ? a.getCategory().getSlug() : null)
                .tagSlugs(a.getTags() != null
                        ? a.getTags().stream().map(t -> t.getSlug()).collect(Collectors.toList())
                        : List.of())
                .tagNames(a.getTags() != null
                        ? a.getTags().stream().map(t -> t.getName()).collect(Collectors.toList())
                        : List.of())
                .breaking(a.isBreaking())
                .featured(a.isFeatured())
                .viewCount(a.getViewCount())
                .publishedAt(a.getPublishedAt())
                .build();
    }

    private ArticleResponse toResponse(ArticleDocument doc) {
        CategoryResponse category = null;
        if (doc.getCategorySlug() != null) {
            category = CategoryResponse.builder()
                    .name(doc.getCategoryName())
                    .nameBn(doc.getCategoryNameBn())
                    .slug(doc.getCategorySlug())
                    .build();
        }

        UserResponse author = null;
        if (doc.getAuthorId() != null) {
            author = UserResponse.builder()
                    .id(UUID.fromString(doc.getAuthorId()))
                    .fullName(doc.getAuthorFullName())
                    .build();
        }

        return ArticleResponse.builder()
                .id(UUID.fromString(doc.getId()))
                .title(doc.getTitle())
                .titleBn(doc.getTitleBn())
                .slug(doc.getSlug())
                .summary(doc.getSummary())
                .summaryBn(doc.getSummaryBn())
                .featuredImage(doc.getFeaturedImage())
                .author(author)
                .category(category)
                .breaking(doc.isBreaking())
                .featured(doc.isFeatured())
                .viewCount(doc.getViewCount())
                .publishedAt(doc.getPublishedAt())
                .build();
    }
}
