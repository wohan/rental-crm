package ru.rentalcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentalcrm.model.Reminder;
import ru.rentalcrm.model.ReminderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findTop50ByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(ReminderStatus status, OffsetDateTime scheduledAt);
}
