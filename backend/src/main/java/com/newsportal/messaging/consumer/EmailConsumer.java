package com.newsportal.messaging.consumer;

import com.newsportal.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.Q_EMAIL)
    public void handleEmail(Map<String, Object> event) {
        String to      = (String) event.get("to");
        String subject = (String) event.get("subject");
        String body    = (String) event.get("body");

        if (to == null || subject == null || body == null) {
            log.warn("Invalid email event, missing fields: {}", event);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw e; // will be dead-lettered after retry
        }
    }
}
