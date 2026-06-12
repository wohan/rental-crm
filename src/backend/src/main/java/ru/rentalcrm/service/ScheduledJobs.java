package ru.rentalcrm.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ScheduledJobs {
    private final PaymentService paymentService;
    private final ReminderDispatcher reminderDispatcher;

    public ScheduledJobs(PaymentService paymentService, ReminderDispatcher reminderDispatcher) {
        this.paymentService = paymentService;
        this.reminderDispatcher = reminderDispatcher;
    }

    @Scheduled(cron = "0 10 6 * * *")
    public void dailyPaymentStatusRefresh() {
        paymentService.markOverduePayments(LocalDate.now());
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void dispatchReminders() {
        reminderDispatcher.dispatchDueReminders();
    }
}
