package com.newsportal.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE = "news.portal.exchange";

    // Routing keys
    public static final String RK_ARTICLE_PUBLISHED  = "article.published";
    public static final String RK_ARTICLE_VIEWED     = "article.viewed";
    public static final String RK_COMMENT_CREATED    = "comment.created";
    public static final String RK_MEDIA_UPLOADED     = "media.uploaded";
    public static final String RK_NOTIFICATION_EMAIL = "notification.email";
    public static final String RK_NOTIFICATION_PUSH  = "notification.push";

    // Queue names
    public static final String Q_ARTICLE_PUBLISHED  = "article.published.queue";
    public static final String Q_ARTICLE_VIEWED     = "article.viewed.queue";
    public static final String Q_COMMENT_MODERATION = "comment.moderation.queue";
    public static final String Q_MEDIA_PROCESSING   = "media.processing.queue";
    public static final String Q_EMAIL              = "email.queue";
    public static final String Q_PUSH               = "push.queue";

    // Dead-letter queues
    public static final String Q_ARTICLE_PUBLISHED_DLQ  = "article.published.dlq";
    public static final String Q_EMAIL_DLQ               = "email.dlq";

    @Bean
    public TopicExchange newsPortalExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    // Dead-letter exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("news.portal.dlx").durable(true).build();
    }

    @Bean
    public Queue articlePublishedQueue() {
        return QueueBuilder.durable(Q_ARTICLE_PUBLISHED)
                .withArgument("x-dead-letter-exchange", "news.portal.dlx")
                .withArgument("x-dead-letter-routing-key", Q_ARTICLE_PUBLISHED_DLQ)
                .build();
    }

    @Bean
    public Queue articleViewedQueue() {
        return QueueBuilder.durable(Q_ARTICLE_VIEWED).build();
    }

    @Bean
    public Queue commentModerationQueue() {
        return QueueBuilder.durable(Q_COMMENT_MODERATION).build();
    }

    @Bean
    public Queue mediaProcessingQueue() {
        return QueueBuilder.durable(Q_MEDIA_PROCESSING).build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(Q_EMAIL)
                .withArgument("x-dead-letter-exchange", "news.portal.dlx")
                .withArgument("x-dead-letter-routing-key", Q_EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable(Q_PUSH).build();
    }

    @Bean
    public Queue articlePublishedDlq() {
        return QueueBuilder.durable(Q_ARTICLE_PUBLISHED_DLQ).build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(Q_EMAIL_DLQ).build();
    }

    @Bean
    public Binding bindArticlePublished(Queue articlePublishedQueue, TopicExchange newsPortalExchange) {
        return BindingBuilder.bind(articlePublishedQueue).to(newsPortalExchange).with(RK_ARTICLE_PUBLISHED);
    }

    @Bean
    public Binding bindArticleViewed(Queue articleViewedQueue, TopicExchange newsPortalExchange) {
        return BindingBuilder.bind(articleViewedQueue).to(newsPortalExchange).with(RK_ARTICLE_VIEWED);
    }

    @Bean
    public Binding bindCommentModeration(Queue commentModerationQueue, TopicExchange newsPortalExchange) {
        return BindingBuilder.bind(commentModerationQueue).to(newsPortalExchange).with(RK_COMMENT_CREATED);
    }

    @Bean
    public Binding bindMediaProcessing(Queue mediaProcessingQueue, TopicExchange newsPortalExchange) {
        return BindingBuilder.bind(mediaProcessingQueue).to(newsPortalExchange).with(RK_MEDIA_UPLOADED);
    }

    @Bean
    public Binding bindEmail(Queue emailQueue, TopicExchange newsPortalExchange) {
        return BindingBuilder.bind(emailQueue).to(newsPortalExchange).with(RK_NOTIFICATION_EMAIL);
    }

    @Bean
    public Binding bindPush(Queue pushQueue, TopicExchange newsPortalExchange) {
        return BindingBuilder.bind(pushQueue).to(newsPortalExchange).with(RK_NOTIFICATION_PUSH);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
