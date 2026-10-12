package com.queueless.repository;

import com.queueless.model.QueueEntry;
import com.queueless.model.QueueStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    // highest token number used so far in a service (to make the next one)
    Optional<QueueEntry> findTopByServiceIdOrderByTokenNumberDesc(Long serviceId);

    // does this user already have an active token in this service?
    boolean existsByUserIdAndServiceIdAndStatusIn(Long userId, Long serviceId, List<QueueStatus> statuses);

    List<QueueEntry> findByUserIdOrderByJoinedAtDesc(Long userId);

    // how many people have this status in a service
    long countByServiceIdAndStatus(Long serviceId, QueueStatus status);

    // how many WAITING people have a smaller token number (= people ahead of you)
    long countByServiceIdAndStatusAndTokenNumberLessThan(Long serviceId, QueueStatus status, int tokenNumber);

    // the queue of a service, in token order
    List<QueueEntry> findByServiceIdAndStatusInOrderByTokenNumberAsc(Long serviceId, List<QueueStatus> statuses);

    // the next person to call
    Optional<QueueEntry> findFirstByServiceIdAndStatusOrderByTokenNumberAsc(Long serviceId, QueueStatus status);

    // tokens that are waiting and have not received the 10-minute reminder yet
    List<QueueEntry> findByStatusAndNotifiedFalse(QueueStatus status);

    // ----- work history -----

    // history shown to one staff member (latest 200, newest first, not cleared by them)
    List<QueueEntry> findTop200ByServedByIdAndStatusInAndHiddenFromStaffFalseOrderByFinishedAtDesc(
            Long staffId, List<QueueStatus> statuses);

    // all history rows of one staff member that are still visible (used by "clear history")
    List<QueueEntry> findByServedByIdAndStatusInAndHiddenFromStaffFalse(Long staffId, List<QueueStatus> statuses);

    // history of ALL staff for the admin (latest 200, newest first)
    List<QueueEntry> findTop200ByStatusInOrderByFinishedAtDesc(List<QueueStatus> statuses);
}
