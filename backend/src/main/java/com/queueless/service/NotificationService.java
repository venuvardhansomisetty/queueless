package com.queueless.service;

import com.queueless.dto.Dtos.*;
import com.queueless.model.Notification;
import com.queueless.model.User;
import com.queueless.repository.NotificationRepository;
import com.queueless.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Saves in-app notifications and also sends the same message by email.
@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepo, UserRepository userRepo,
                               EmailService emailService) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    // Called from the queue code and from the scheduler
    public void notifyUser(User user, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        notificationRepo.save(n);

        emailService.send(user.getEmail(), "QueueLess update", message);
    }

    public List<NotificationResponse> getMyNotifications(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void markAllRead(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Notification> unread = notificationRepo.findByUserIdAndReadFalse(user.getId());
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepo.saveAll(unread);
    }
}
