package com.newsportal.domain.category;

import com.newsportal.common.exception.ResourceNotFoundException;
import com.newsportal.domain.category.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable("categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findTopLevelCategories().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Category findBySlugOrThrow(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .nameBn(c.getNameBn())
                .slug(c.getSlug())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .sortOrder(c.getSortOrder())
                .children(c.getChildren().stream().map(this::toResponse).toList())
                .build();
    }
}
