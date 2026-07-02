package ru.rentcrm.app.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.model.Listing;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.ListingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    private final CurrentUser current;
    private final ListingService service;

    public ListingController(CurrentUser current, ListingService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<Listing> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public Listing create(@RequestBody Listing item) {
        return service.create(current.get(), item);
    }

    @PutMapping("/{id}")
    public Listing update(@PathVariable UUID id, @RequestBody Listing item) {
        return service.update(current.get(), id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
