package com.newsportal.domain.tag.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {
    private Integer id;
    private String name;
    private String nameBn;
    private String slug;
}
