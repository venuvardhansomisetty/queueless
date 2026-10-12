package com.queueless.service;

import com.queueless.dto.Dtos.*;
import com.queueless.model.Role;
import com.queueless.model.ServiceItem;
import com.queueless.model.User;
import com.queueless.repository.ServiceItemRepository;
import com.queueless.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Things only the admin can do: create services, create staff, assign staff, watch queues.
@Service
public class AdminService {

    private final ServiceItemRepository serviceRepo;
    private final UserRepository userRepo;
    private final AuthService authService;
    private final QueueService queueService;

    public AdminService(ServiceItemRepository serviceRepo, UserRepository userRepo,
                        AuthService authService, QueueService queueService) {
        this.serviceRepo = serviceRepo;
        this.userRepo = userRepo;
        this.authService = authService;
        this.queueService = queueService;
    }

    public ServiceResponse createService(ServiceRequest request) {
        ServiceItem service = new ServiceItem();
        service.setName(request.name().trim());
        service.setDescription(request.description());
        // if the admin leaves the time empty we use 5 minutes
        service.setAvgServiceMinutes(request.avgServiceMinutes() == null ? 5 : request.avgServiceMinutes());
        serviceRepo.save(service);
        return queueService.toServiceResponse(service);
    }

    public List<ServiceResponse> listServices() {
        return queueService.listServices();
    }

    public ServiceResponse assignStaff(Long serviceId, Long staffId) {
        ServiceItem service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        User staff = userRepo.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found"));

        if (staff.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Selected user is not a staff member");
        }
        service.setStaff(staff);
        serviceRepo.save(service);
        return queueService.toServiceResponse(service);
    }

    public UserResponse createStaff(RegisterRequest request) {
        User user = authService.createUser(request.name(), request.email(), request.password(), Role.STAFF);
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public List<UserResponse> listStaff() {
        return userRepo.findByRole(Role.STAFF).stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name()))
                .toList();
    }

    public List<QueueEntryResponse> getQueue(Long serviceId) {
        return queueService.getQueue(serviceId);
    }

    public List<HistoryResponse> getHistory() {
        return queueService.adminHistory();
    }
}
