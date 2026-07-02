package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "error_events")
public class ErrorEvent {
    @Id
    public UUID id = UUID.randomUUID();

    public UUID accountId;
    public String actorEmail;
    public String method;
    public String path;
    public String exceptionClass;

    @Column(columnDefinition = "text")
    public String message;

    @Column(columnDefinition = "text")
    public String stackTrace;

    public Instant createdAt = Instant.now();
    public Boolean notified = false;
}
