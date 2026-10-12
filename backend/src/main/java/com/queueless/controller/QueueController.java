package com.queueless.controller;

import com.queueless.dto.Dtos.*;
import com.queueless.service.QueueService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Student APIs. "Authentication" gives us the logged-in user (from the JWT). auth.getName() = the email.
@RestController
@RequestMapping("/api")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/services")
    public List<ServiceResponse> services() {
        return queueService.listServices();
    }

    @PostMapping("/queue/join/{serviceId}")
    public QueueEntryResponse join(@PathVariable Long serviceId, Authentication auth) {
        return queueService.join(auth.getName(), serviceId);
    }

    @GetMapping("/queue/my")
    public List<QueueEntryResponse> myTokens(Authentication auth) {
        return queueService.myTokens(auth.getName());
    }

    @PutMapping("/queue/cancel/{entryId}")
    public QueueEntryResponse cancel(@PathVariable Long entryId, Authentication auth) {
        return queueService.cancel(auth.getName(), entryId);
    }
}
