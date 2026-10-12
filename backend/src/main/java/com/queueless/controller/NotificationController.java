package com.queueless.controller;

import com.queueless.dto.Dtos.*;
import com.queueless.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> myNotifications(Authentication auth) {
        return notificationService.getMyNotifications(auth.getName());
    }

    @PutMapping("/read-all")
    public MessageResponse markAllRead(Authentication auth) {
        notificationService.markAllRead(auth.getName());
        return new MessageResponse("All notifications marked as read");
    }
}
