package com.sameer.job.controller;

import com.sameer.job.event.ApplicationStatusChangedEvent;
import com.sameer.job.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailNotificationService emailNotificationService;

    @GetMapping("/sent")
    public String NotificationController() throws Exception {
//        emailNotificationService.sendStatusChangedEmail();
        return "email sent";
    }
}
