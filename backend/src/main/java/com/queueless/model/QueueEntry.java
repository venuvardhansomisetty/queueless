package com.queueless.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// One token. When a student joins a queue, one row is created here.
// The unique constraint is our safety net: the same token number can never repeat inside one service.
@Entity
@Table(name = "queue_entries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "token_number"}))
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false)
    private int tokenNumber;

    // Many tokens belong to one user
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // Many tokens belong to one service
    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id")
    private ServiceItem service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.WAITING;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    // The staff member who handled this token (set when called or skipped). Used for work history.
    @ManyToOne
    @JoinColumn(name = "served_by_id")
    private User servedBy;

    // When the token ended (completed or skipped)
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    // "Clear history" on the staff page only HIDES the row from that staff member.
    // The admin can still see it, so the work record is never lost.
    @Column(name = "hidden_from_staff", nullable = false, columnDefinition = "boolean default false")
    private boolean hiddenFromStaff = false;

    // true after we have sent the "your turn is in about 10 minutes" message (so we send it only once)
    private boolean notified = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(int tokenNumber) { this.tokenNumber = tokenNumber; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ServiceItem getService() { return service; }
    public void setService(ServiceItem service) { this.service = service; }
    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }
    public User getServedBy() { return servedBy; }
    public void setServedBy(User servedBy) { this.servedBy = servedBy; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public boolean isHiddenFromStaff() { return hiddenFromStaff; }
    public void setHiddenFromStaff(boolean hiddenFromStaff) { this.hiddenFromStaff = hiddenFromStaff; }
    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
