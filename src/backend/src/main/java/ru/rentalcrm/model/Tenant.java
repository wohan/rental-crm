package ru.rentalcrm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    private LegalType legalType = LegalType.INDIVIDUAL;

    private String inn;
    private String passportEncrypted;
    private String notes;
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LegalType getLegalType() { return legalType; }
    public void setLegalType(LegalType legalType) { this.legalType = legalType; }
    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }
    public String getPassportEncrypted() { return passportEncrypted; }
    public void setPassportEncrypted(String passportEncrypted) { this.passportEncrypted = passportEncrypted; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
