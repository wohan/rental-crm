package ru.rentcrm.app.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.RentalObject;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.ObjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/objects")
public class ObjectController {
    private final CurrentUser current;
    private final ObjectService service;

    public ObjectController(CurrentUser current, ObjectService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<RentalObject> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public RentalObject create(@Valid @RequestBody RentalObject item) {
        return service.create(current.get(), item);
    }

    @PutMapping("/{id}")
    public RentalObject update(@PathVariable UUID id, @RequestBody RentalObject item) {
        return service.update(current.get(), id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
