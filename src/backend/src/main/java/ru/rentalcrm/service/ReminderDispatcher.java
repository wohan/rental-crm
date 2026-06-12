package ru.rentalcrm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rentalcrm.model.ReminderStatus;
import ru.rentalcrm.repository.ReminderRepository;

import java.time.OffsetDateTime;

@Service
public class ReminderDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ReminderDispatcher.class);
    private final ReminderRepository reminderRepository;
    private final boolean dryRun;

    public ReminderDispatcher(ReminderRepository reminderRepository,
                              @Value("${app.notification-dry-run:true}") boolean dryRun) {
        this.reminderRepository = reminderRepository;
        this.dryRun = dryRun;
    }

    @Transactional
    public int dispatchDueReminders() {
        var reminders = reminderRepository.findTop50ByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
                ReminderStatus.PENDING, OffsetDateTime.now());
        reminders.forEach(reminder -> {
            if (dryRun) {
                log.info("Dry-run notification: channel={}, type={}, payload={}",
                        reminder.getChannel(), reminder.getType(), reminder.getPayload());
            }
            reminder.setStatus(ReminderStatus.SENT);
            reminder.setSentAt(OffsetDateTime.now());
        });
        reminderRepository.saveAll(reminders);
        return reminders.size();
    }
}
