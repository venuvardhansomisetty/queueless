package com.queueless.service;

import com.queueless.model.QueueEntry;
import com.queueless.model.QueueStatus;
import com.queueless.repository.QueueEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// Runs by itself every 1 minute and sends the "your turn is coming" reminder.
@Component
public class QueueScheduler {

    private static final Logger log = LoggerFactory.getLogger(QueueScheduler.class);

    private static final int REMINDER_MINUTES = 10;

    private final QueueEntryRepository queueRepo;
    private final QueueService queueService;
    private final NotificationService notificationService;

    public QueueScheduler(QueueEntryRepository queueRepo, QueueService queueService,
                          NotificationService notificationService) {
        this.queueRepo = queueRepo;
        this.queueService = queueService;
        this.notificationService = notificationService;
    }

    // fixedDelay = wait 60,000 ms (1 minute) after the previous run finishes, then run again
    @Scheduled(fixedDelay = 60000)
    public void sendReminders() {
        List<QueueEntry> waiting = queueRepo.findByStatusAndNotifiedFalse(QueueStatus.WAITING);

        for (QueueEntry entry : waiting) {
            try {
                int wait = queueService.estimateWait(entry);

                if (wait <= REMINDER_MINUTES) {
                    String serviceName = entry.getService().getName();
                    String message;
                    if (wait == 0) {
                        message = "You are next in line for " + serviceName + " (token #"
                                + entry.getTokenNumber() + "). Please be at the counter now.";
                    } else {
                        message = "Your token #" + entry.getTokenNumber() + " for " + serviceName
                                + " will come in about " + wait + " minutes. Please be at the counter on time.";
                    }
                    notificationService.notifyUser(entry.getUser(), message);

                    entry.setNotified(true); // so we do not send it again next minute
                    queueRepo.save(entry);
                }
            } catch (Exception e) {
                log.warn("Reminder failed for token {}: {}", entry.getId(), e.getMessage());
            }
        }
    }
}
