package com.sameer.job.event;

import com.sameer.job.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final EmailNotificationService emailService;

    @KafkaListener(
            topics = "application.status.changed",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStatusChanged(ApplicationStatusChangedEvent event) throws Exception {
        log.info("Received application status change for application {}", event.getApplicationId());
        emailService.sendStatusChangedEmail(event);
    }
}