package com.newsportal.scheduler;

import com.newsportal.domain.article.Article;
import com.newsportal.domain.article.ArticleRepository;
import com.newsportal.domain.article.ArticleStatus;
import com.newsportal.messaging.consumer.ArticleEventConsumer;
import com.newsportal.messaging.publisher.ArticleEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

    private final ArticleRepository articleRepository;
    private final ArticleEventPublisher eventPublisher;
    private final ArticleEventConsumer articleEventConsumer;

    /**
     * Every minute: publish scheduled articles whose time has come.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void publishScheduledArticles() {
        List<Article> due = articleRepository.findScheduledArticlesDue(Instant.now());
        if (due.isEmpty()) return;

        log.info("Publishing {} scheduled articles", due.size());
        for (Article article : due) {
            article.setStatus(ArticleStatus.PUBLISHED);
            article.setPublishedAt(Instant.now());
            articleRepository.save(article);
            eventPublisher.publishArticlePublishedEvent(article);
        }
    }

    /**
     * Every 30 seconds: flush batched view counts to DB.
     */
    @Scheduled(fixedDelay = 30_000)
    public void flushViewCounts() {
        articleEventConsumer.flushViewCounts();
    }
}
