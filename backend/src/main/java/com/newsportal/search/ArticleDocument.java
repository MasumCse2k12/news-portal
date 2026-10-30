package com.newsportal.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.util.List;

@Document(indexName = "articles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDocument {

    @Id
    private String id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String titleBn;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summaryBn;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Keyword, index = false)
    private String featuredImage;

    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Text)
    private String authorFullName;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String categoryNameBn;

    @Field(type = FieldType.Keyword)
    private String categorySlug;

    @Field(type = FieldType.Keyword)
    private List<String> tagSlugs;

    @Field(type = FieldType.Keyword)
    private List<String> tagNames;

    @Field(type = FieldType.Boolean)
    private boolean breaking;

    @Field(type = FieldType.Boolean)
    private boolean featured;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant publishedAt;
}
