package ru.rentcrm.app.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.AccountEntity;
import ru.rentcrm.app.model.DocumentItem;

@Service
public class OwnershipService {
  public <T extends AccountEntity> T requireOwned(Optional<T> entity, UUID accountId) {
    T item = entity.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Запись не найдена"));
    if (!item.accountId.equals(accountId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Запись не найдена");
    }
    return item;
  }

  public DocumentItem requireOwnedDocument(Optional<DocumentItem> entity, UUID accountId) {
    DocumentItem item =
        entity.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Документ не найден"));
    if (!item.accountId.equals(accountId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Документ не найден");
    }
    return item;
  }
}
