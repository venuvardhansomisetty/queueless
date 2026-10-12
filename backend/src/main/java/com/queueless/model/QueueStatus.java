package com.queueless.model;

// Life of one token: WAITING -> CALLED -> COMPLETED
// A token can also end as CANCELLED (student cancelled) or SKIPPED (staff skipped).
public enum QueueStatus {
    WAITING, CALLED, COMPLETED, CANCELLED, SKIPPED
}
