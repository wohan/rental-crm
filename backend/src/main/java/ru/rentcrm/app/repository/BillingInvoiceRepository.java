package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.BillingInvoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, UUID> {
    List<BillingInvoice> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    Optional<BillingInvoice> findByProviderPaymentId(String providerPaymentId);
}
