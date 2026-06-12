package ru.rentalcrm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "leases")
public class Lease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id")
    private RentalProperty property;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private BigDecimal monthlyRent;
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Min(1)
    @Max(28)
    private Integer paymentDay;

    @Enumerated(EnumType.STRING)
    private LeaseStatus status = LeaseStatus.ACTIVE;

    private String contractNumber;
    private String contractFileUrl;
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RentalProperty getProperty() { return property; }
    public void setProperty(RentalProperty property) { this.property = property; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public Integer getPaymentDay() { return paymentDay; }
    public void setPaymentDay(Integer paymentDay) { this.paymentDay = paymentDay; }
    public LeaseStatus getStatus() { return status; }
    public void setStatus(LeaseStatus status) { this.status = status; }
    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }
    public String getContractFileUrl() { return contractFileUrl; }
    public void setContractFileUrl(String contractFileUrl) { this.contractFileUrl = contractFileUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
