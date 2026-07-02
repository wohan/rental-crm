package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.rentcrm.app.model.NotificationChannel;
import ru.rentcrm.app.model.NotificationDelivery;

import java.util.Optional;
import java.util.UUID;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
    @Query("""
        select count(d) > 0
        from NotificationDelivery d
        where d.paymentId = :paymentId
          and d.channel = :channel
          and d.remindDaysBefore = :remindDaysBefore
        """)
    boolean existsByPaymentIdAndChannelAndRemindDaysBefore(UUID paymentId, NotificationChannel channel, Integer remindDaysBefore);

    @Query("""
        select d
        from NotificationDelivery d
        where d.paymentId = :paymentId
          and d.channel = :channel
          and d.remindDaysBefore = :remindDaysBefore
        """)
    Optional<NotificationDelivery> findExisting(UUID paymentId, NotificationChannel channel, Integer remindDaysBefore);
}
