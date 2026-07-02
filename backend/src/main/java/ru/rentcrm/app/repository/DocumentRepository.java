package ru.rentcrm.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.DocumentItem;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentItem, UUID> {
    List<DocumentItem> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
