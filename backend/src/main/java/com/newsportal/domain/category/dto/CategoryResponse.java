package com.newsportal.domain.category.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryResponse {
    private Integer id;
    private String name;
    private String nameBn;
    private String slug;
    private Integer parentId;
    private Integer sortOrder;
    private List<CategoryResponse> children;
}
