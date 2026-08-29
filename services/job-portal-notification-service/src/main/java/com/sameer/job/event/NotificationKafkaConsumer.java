package com.sameer.job.event;

import com.sameer.job.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
        System.out.println("Received -------- " + event);
        emailService.sendStatusChangedEmail(event);
    }
}