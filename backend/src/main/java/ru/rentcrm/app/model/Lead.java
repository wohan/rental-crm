package ru.rentcrm.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "leads")
public class Lead extends AccountEntity {
    public UUID listingId;
    public UUID objectId;

    @Column(nullable = false)
    public String fullName;

    @Column(nullable = false)
    public String phone;

    public String email;
    public String source;
    public String status = "NEW";

    @Column(columnDefinition = "text")
    public String comment;
}
