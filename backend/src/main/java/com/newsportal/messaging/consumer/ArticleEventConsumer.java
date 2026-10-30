package com.newsportal.messaging.consumer;

import com.newsportal.config.RabbitMQConfig;
import com.newsportal.domain.article.Article;
import com.newsportal.domain.article.ArticleRepository;
import com.newsportal.search.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Consumes article events from RabbitMQ.
 * View counts are batched in memory for 30s before a bulk DB update
 * to avoid per-request DB writes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventConsumer {

    private final ArticleRepository articleRepository;
    private final SearchService searchService;

    // In-memory view count buffer: articleId -> count
    private final ConcurrentHashMap<UUID, AtomicLong> viewCountBuffer = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitMQConfig.Q_ARTICLE_PUBLISHED)
    public void handleArticlePublished(Map<String, Object> event) {
        String articleIdStr = (String) event.get("articleId");
        String slug = (String) event.get("slug");
        log.info("Article published event received: slug={}", slug);

        Optional<Article> article = articleRepository.findById(UUID.fromString(articleIdStr));
        if (article.isPresent()) {
            searchService.indexArticle(article.get());
        } else {
            log.warn("Article not found for indexing: id={}", articleIdStr);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_ARTICLE_VIEWED)
    public void handleArticleViewed(Map<String, Object> event) {
        String articleIdStr = (String) event.get("articleId");
        if (articleIdStr == null) return;

        UUID articleId = UUID.fromString(articleIdStr);
        viewCountBuffer.computeIfAbsent(articleId, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Flush view count buffer to DB every 30 seconds.
     * Called by the ArticleScheduler.
     */
    public void flushViewCounts() {
        if (viewCountBuffer.isEmpty()) return;

        viewCountBuffer.forEach((articleId, count) -> {
            long delta = count.getAndSet(0);
            if (delta > 0) {
                try {
                    articleRepository.incrementViewCount(articleId, delta);
                } catch (Exception e) {
                    log.error("Failed to flush view count for article: {}", articleId, e);
                    count.addAndGet(delta); // put it back
                }
            }
        });

        log.debug("View count buffer flushed for {} articles", viewCountBuffer.size());
    }
}
