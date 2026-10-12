package com.queueless.service;

import com.queueless.dto.Dtos.*;
import com.queueless.dto.Dtos.HistoryResponse;
import com.queueless.dto.Dtos.QueueEntryResponse;
import com.queueless.dto.Dtos.ServiceResponse;
import com.queueless.model.QueueEntry;
import com.queueless.model.QueueStatus;
import com.queueless.model.ServiceItem;
import com.queueless.model.User;
import com.queueless.repository.QueueEntryRepository;
import com.queueless.repository.ServiceItemRepository;
import com.queueless.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// The heart of QueueLess: join, cancel, call next, complete, skip, position and waiting time.
@Service
public class QueueService {

    // a token is "active" while it is waiting or being served
    private static final List<QueueStatus> ACTIVE = List.of(QueueStatus.WAITING, QueueStatus.CALLED);

    // a token is "finished staff work" when staff completed it or skipped it
    private static final List<QueueStatus> FINISHED_BY_STAFF = List.of(QueueStatus.COMPLETED, QueueStatus.SKIPPED);

    private final QueueEntryRepository queueRepo;
    private final ServiceItemRepository serviceRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public QueueService(QueueEntryRepository queueRepo, ServiceItemRepository serviceRepo,
                        UserRepository userRepo, NotificationService notificationService) {
        this.queueRepo = queueRepo;
        this.serviceRepo = serviceRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    // =====================================================
    // Small helper methods
    // =====================================================

    private User getUser(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private QueueEntry getEntry(Long entryId) {
        return queueRepo.findById(entryId).orElseThrow(() -> new IllegalArgumentException("Token not found"));
    }

    // staff can only manage the services the admin assigned to them
    private void checkOwner(ServiceItem service, User staff) {
        if (service.getStaff() == null || !service.getStaff().getId().equals(staff.getId())) {
            throw new IllegalArgumentException("This service is not assigned to you");
        }
    }

    // how many WAITING people have a smaller token number than this one
    private long peopleAhead(QueueEntry e) {
        return queueRepo.countByServiceIdAndStatusAndTokenNumberLessThan(
                e.getService().getId(), QueueStatus.WAITING, e.getTokenNumber());
    }

    // Estimated waiting time (minutes) = (people ahead + person being served now) x average time per person
    public int estimateWait(QueueEntry e) {
        if (e.getStatus() != QueueStatus.WAITING) {
            return 0;
        }
        long ahead = peopleAhead(e);
        long beingServed = queueRepo.countByServiceIdAndStatus(e.getService().getId(), QueueStatus.CALLED);
        return (int) ((ahead + beingServed) * e.getService().getAvgServiceMinutes());
    }

    public ServiceResponse toServiceResponse(ServiceItem s) {
        long waiting = queueRepo.countByServiceIdAndStatus(s.getId(), QueueStatus.WAITING);
        Long staffId = s.getStaff() == null ? null : s.getStaff().getId();
        String staffName = s.getStaff() == null ? null : s.getStaff().getName();
        return new ServiceResponse(s.getId(), s.getName(), s.getDescription(),
                s.getAvgServiceMinutes(), staffId, staffName, waiting);
    }

    public QueueEntryResponse toEntryResponse(QueueEntry e) {
        // your position = people ahead of you + 1 (only for waiting tokens)
        int position = e.getStatus() == QueueStatus.WAITING ? (int) peopleAhead(e) + 1 : 0;
        return new QueueEntryResponse(e.getId(), e.getTokenNumber(), e.getService().getId(),
                e.getService().getName(), e.getUser().getName(), e.getStatus().name(),
                e.getJoinedAt(), position, estimateWait(e));
    }

    // =====================================================
    // Student
    // =====================================================

    public List<ServiceResponse> listServices() {
        return serviceRepo.findAll().stream().map(this::toServiceResponse).toList();
    }

    @Transactional
    public QueueEntryResponse join(String email, Long serviceId) {
        User user = getUser(email);

        // Lock this service row. Another student joining at the same moment must wait for us.
        ServiceItem service = serviceRepo.findByIdForUpdate(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        // Rule: only one active token per person per service
        if (queueRepo.existsByUserIdAndServiceIdAndStatusIn(user.getId(), serviceId, ACTIVE)) {
            throw new IllegalArgumentException("You already have an active token for this service");
        }

        // next token = last token + 1 (or 1 if the queue is new)
        int nextToken = queueRepo.findTopByServiceIdOrderByTokenNumberDesc(serviceId)
                .map(last -> last.getTokenNumber() + 1)
                .orElse(1);

        QueueEntry entry = new QueueEntry();
        entry.setUser(user);
        entry.setService(service);
        entry.setTokenNumber(nextToken);
        queueRepo.save(entry);

        return toEntryResponse(entry);
    }

    public List<QueueEntryResponse> myTokens(String email) {
        User user = getUser(email);
        return queueRepo.findByUserIdOrderByJoinedAtDesc(user.getId()).stream()
                .map(this::toEntryResponse).toList();
    }

    @Transactional
    public QueueEntryResponse cancel(String email, Long entryId) {
        User user = getUser(email);
        QueueEntry entry = getEntry(entryId);

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("This is not your token");
        }
        if (entry.getStatus() != QueueStatus.WAITING) {
            throw new IllegalArgumentException("Only a waiting token can be cancelled");
        }
        entry.setStatus(QueueStatus.CANCELLED);
        queueRepo.save(entry);
        return toEntryResponse(entry);
    }

    // =====================================================
    // Staff
    // =====================================================

    public List<ServiceResponse> staffServices(String email) {
        User staff = getUser(email);
        return serviceRepo.findByStaffId(staff.getId()).stream().map(this::toServiceResponse).toList();
    }

    // the people currently waiting or being served in one service (also used by the admin)
    public List<QueueEntryResponse> getQueue(Long serviceId) {
        return queueRepo.findByServiceIdAndStatusInOrderByTokenNumberAsc(serviceId, ACTIVE).stream()
                .map(this::toEntryResponse).toList();
    }

    public List<QueueEntryResponse> staffQueue(String email, Long serviceId) {
        User staff = getUser(email);
        ServiceItem service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        checkOwner(service, staff);
        return getQueue(serviceId);
    }

    @Transactional
    public QueueEntryResponse callNext(String email, Long serviceId) {
        User staff = getUser(email);
        // lock the service so two quick clicks cannot call two people
        ServiceItem service = serviceRepo.findByIdForUpdate(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        checkOwner(service, staff);

        if (queueRepo.countByServiceIdAndStatus(serviceId, QueueStatus.CALLED) > 0) {
            throw new IllegalArgumentException("Complete or skip the person being served first");
        }

        QueueEntry next = queueRepo.findFirstByServiceIdAndStatusOrderByTokenNumberAsc(serviceId, QueueStatus.WAITING)
                .orElseThrow(() -> new IllegalArgumentException("No one is waiting in this queue"));

        next.setStatus(QueueStatus.CALLED);
        next.setCalledAt(LocalDateTime.now());
        next.setServedBy(staff); // remember who served this person (for the history)
        queueRepo.save(next);

        notificationService.notifyUser(next.getUser(), "It's your turn! Token #" + next.getTokenNumber()
                + " for " + service.getName() + ". Please go to the counter now.");

        return toEntryResponse(next);
    }

    @Transactional
    public QueueEntryResponse complete(String email, Long entryId) {
        User staff = getUser(email);
        QueueEntry entry = getEntry(entryId);
        checkOwner(entry.getService(), staff);

        if (entry.getStatus() != QueueStatus.CALLED) {
            throw new IllegalArgumentException("Only the person being served can be completed");
        }
        entry.setStatus(QueueStatus.COMPLETED);
        entry.setServedBy(staff);
        entry.setFinishedAt(LocalDateTime.now());
        queueRepo.save(entry);
        return toEntryResponse(entry);
    }

    @Transactional
    public QueueEntryResponse skip(String email, Long entryId) {
        User staff = getUser(email);
        QueueEntry entry = getEntry(entryId);
        checkOwner(entry.getService(), staff);

        if (entry.getStatus() != QueueStatus.WAITING && entry.getStatus() != QueueStatus.CALLED) {
            throw new IllegalArgumentException("This token is already finished");
        }
        entry.setStatus(QueueStatus.SKIPPED);
        entry.setServedBy(staff);
        entry.setFinishedAt(LocalDateTime.now());
        queueRepo.save(entry);

        notificationService.notifyUser(entry.getUser(), "Your token #" + entry.getTokenNumber()
                + " for " + entry.getService().getName()
                + " was skipped because you were not at the counter. You can join the queue again.");

        return toEntryResponse(entry);
    }

    // =====================================================
    // Work history
    // =====================================================

    public HistoryResponse toHistoryResponse(QueueEntry e) {
        // minutes the student waited before being called (empty if the person was skipped while waiting)
        Long waited = e.getCalledAt() == null ? null
                : Duration.between(e.getJoinedAt(), e.getCalledAt()).toMinutes();
        // minutes the staff member spent serving
        Long served = (e.getCalledAt() == null || e.getFinishedAt() == null) ? null
                : Duration.between(e.getCalledAt(), e.getFinishedAt()).toMinutes();

        Long staffId = e.getServedBy() == null ? null : e.getServedBy().getId();
        String staffName = e.getServedBy() == null ? null : e.getServedBy().getName();

        return new HistoryResponse(e.getId(), e.getTokenNumber(), e.getService().getName(),
                e.getUser().getName(), staffId, staffName, e.getStatus().name(),
                e.getJoinedAt(), e.getCalledAt(), e.getFinishedAt(), waited, served);
    }

    // the logged-in staff member's own history
    public List<HistoryResponse> staffHistory(String email) {
        User staff = getUser(email);
        return queueRepo
                .findTop200ByServedByIdAndStatusInAndHiddenFromStaffFalseOrderByFinishedAtDesc(
                        staff.getId(), FINISHED_BY_STAFF)
                .stream().map(this::toHistoryResponse).toList();
    }

    // "Clear history": hide this staff member's rows from THEIR page only. The admin still sees them.
    @Transactional
    public int clearStaffHistory(String email) {
        User staff = getUser(email);
        List<QueueEntry> visible = queueRepo.findByServedByIdAndStatusInAndHiddenFromStaffFalse(
                staff.getId(), FINISHED_BY_STAFF);
        for (QueueEntry e : visible) {
            e.setHiddenFromStaff(true);
        }
        queueRepo.saveAll(visible);
        return visible.size();
    }

    // everything every staff member has done (for the admin)
    public List<HistoryResponse> adminHistory() {
        return queueRepo.findTop200ByStatusInOrderByFinishedAtDesc(FINISHED_BY_STAFF)
                .stream().map(this::toHistoryResponse).toList();
    }
}
