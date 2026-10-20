package ru.rentcrm.app.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.Tenant;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.TenantService;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
  private final CurrentUser current;
  private final TenantService service;

  public TenantController(CurrentUser current, TenantService service) {
    this.current = current;
    this.service = service;
  }

  @GetMapping
  public List<Tenant> list() {
    return service.list(current.accountId());
  }

  @PostMapping
  public Tenant create(@Valid @RequestBody Tenant item) {
    return service.create(current.get(), item);
  }

  @PutMapping("/{id}")
  public Tenant update(@PathVariable UUID id, @Valid @RequestBody Tenant item) {
    return service.update(current.get(), id, item);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(current.get(), id);
  }
}
