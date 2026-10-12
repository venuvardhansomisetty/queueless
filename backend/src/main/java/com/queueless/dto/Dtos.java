package com.queueless.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// DTO = Data Transfer Object. These small classes decide what data comes IN to our APIs
// and what data goes OUT. We never send the User/Entity directly, because it has the password.
// A "record" is a short Java class that only holds data (getters are created automatically).
public class Dtos {

    // ---------- coming IN ----------
    public record RegisterRequest(
            @NotBlank(message = "Name is required") String name,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters") String password) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required") String email,
            @NotBlank(message = "Password is required") String password) {}

    public record ServiceRequest(
            @NotBlank(message = "Service name is required") String name,
            String description,
            @Min(value = 1, message = "Average time must be at least 1 minute") Integer avgServiceMinutes) {}

    public record AssignStaffRequest(@NotNull(message = "Choose a staff member") Long staffId) {}

    // ---------- going OUT ----------
    public record AuthResponse(String token, Long id, String name, String email, String role) {}

    public record UserResponse(Long id, String name, String email, String role) {}

    public record ServiceResponse(Long id, String name, String description, int avgServiceMinutes,
                                  Long staffId, String staffName, long waitingCount) {}

    // position = your place in the line (0 if the token is not waiting)
    public record QueueEntryResponse(Long id, int tokenNumber, Long serviceId, String serviceName,
                                     String userName, String status, LocalDateTime joinedAt,
                                     int position, int estimatedWaitMinutes) {}

    public record NotificationResponse(Long id, String message, boolean read, LocalDateTime createdAt) {}

    // One finished piece of staff work. outcome = COMPLETED or SKIPPED.
    // waitedMinutes = time the student waited before being called, serviceMinutes = time spent serving.
    public record HistoryResponse(Long id, int tokenNumber, String serviceName, String studentName,
                                  Long staffId, String staffName, String outcome,
                                  LocalDateTime joinedAt, LocalDateTime calledAt, LocalDateTime finishedAt,
                                  Long waitedMinutes, Long serviceMinutes) {}

    public record MessageResponse(String message) {}
}
