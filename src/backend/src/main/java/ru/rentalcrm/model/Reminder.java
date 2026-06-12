package ru.rentalcrm.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reminders")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lease_id")
    private Lease lease;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    private ReminderType type;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    private OffsetDateTime scheduledAt;
    private OffsetDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status = ReminderStatus.PENDING;

    private String payload;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Lease getLease() { return lease; }
    public void setLease(Lease lease) { this.lease = lease; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public OffsetDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(OffsetDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
