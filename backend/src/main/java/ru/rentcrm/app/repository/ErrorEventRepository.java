package ru.rentcrm.app.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentcrm.app.model.ErrorEvent;

public interface ErrorEventRepository extends JpaRepository<ErrorEvent, UUID> {
  List<ErrorEvent> findTop100ByOrderByCreatedAtDesc();
}
