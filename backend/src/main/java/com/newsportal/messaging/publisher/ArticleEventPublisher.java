package com.newsportal.messaging.publisher;

import com.newsportal.config.RabbitMQConfig;
import com.newsportal.domain.article.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishArticlePublishedEvent(Article article) {
        Map<String, Object> event = Map.of(
                "articleId", article.getId().toString(),
                "title", article.getTitle(),
                "slug", article.getSlug(),
                "categorySlug", article.getCategory() != null ? article.getCategory().getSlug() : "",
                "publishedAt", article.getPublishedAt().toString()
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_ARTICLE_PUBLISHED,
                    event
            );
            log.debug("Published article.published event for: {}", article.getSlug());
        } catch (Exception e) {
            log.error("Failed to publish article.published event for: {}", article.getSlug(), e);
        }
    }

    @Async
    public void publishViewedEvent(UUID articleId) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_ARTICLE_VIEWED,
                    Map.of("articleId", articleId.toString())
            );
        } catch (Exception e) {
            log.warn("Failed to publish article.viewed event for: {}", articleId, e);
        }
    }
}
