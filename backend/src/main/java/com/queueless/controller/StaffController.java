package com.queueless.controller;

import com.queueless.dto.Dtos.*;
import com.queueless.service.QueueService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Staff APIs (only users with role STAFF can open these urls).
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final QueueService queueService;

    public StaffController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/services")
    public List<ServiceResponse> myServices(Authentication auth) {
        return queueService.staffServices(auth.getName());
    }

    @GetMapping("/services/{serviceId}/queue")
    public List<QueueEntryResponse> queue(@PathVariable Long serviceId, Authentication auth) {
        return queueService.staffQueue(auth.getName(), serviceId);
    }

    @PutMapping("/services/{serviceId}/call-next")
    public QueueEntryResponse callNext(@PathVariable Long serviceId, Authentication auth) {
        return queueService.callNext(auth.getName(), serviceId);
    }

    @PutMapping("/entries/{entryId}/complete")
    public QueueEntryResponse complete(@PathVariable Long entryId, Authentication auth) {
        return queueService.complete(auth.getName(), entryId);
    }

    @PutMapping("/entries/{entryId}/skip")
    public QueueEntryResponse skip(@PathVariable Long entryId, Authentication auth) {
        return queueService.skip(auth.getName(), entryId);
    }

    @GetMapping("/history")
    public List<HistoryResponse> history(Authentication auth) {
        return queueService.staffHistory(auth.getName());
    }

    @PutMapping("/history/clear")
    public MessageResponse clearHistory(Authentication auth) {
        int count = queueService.clearStaffHistory(auth.getName());
        return new MessageResponse("History cleared (" + count + " records hidden)");
    }
}
