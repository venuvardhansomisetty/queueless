package com.queueless.controller;

import com.queueless.dto.Dtos.*;
import com.queueless.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Admin APIs (only users with role ADMIN can open these urls).
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/services")
    public ServiceResponse createService(@Valid @RequestBody ServiceRequest request) {
        return adminService.createService(request);
    }

    @GetMapping("/services")
    public List<ServiceResponse> services() {
        return adminService.listServices();
    }

    @PutMapping("/services/{serviceId}/assign-staff")
    public ServiceResponse assignStaff(@PathVariable Long serviceId, @Valid @RequestBody AssignStaffRequest request) {
        return adminService.assignStaff(serviceId, request.staffId());
    }

    @PostMapping("/staff")
    public UserResponse createStaff(@Valid @RequestBody RegisterRequest request) {
        return adminService.createStaff(request);
    }

    @GetMapping("/staff")
    public List<UserResponse> staff() {
        return adminService.listStaff();
    }

    @GetMapping("/queue/{serviceId}")
    public List<QueueEntryResponse> queue(@PathVariable Long serviceId) {
        return adminService.getQueue(serviceId);
    }

    @GetMapping("/history")
    public List<HistoryResponse> history() {
        return adminService.getHistory();
    }
}
