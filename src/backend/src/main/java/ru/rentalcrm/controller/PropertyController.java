package ru.rentalcrm.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.rentalcrm.model.RentalProperty;
import ru.rentalcrm.repository.PropertyRepository;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {
    private final PropertyRepository repository;

    public PropertyController(PropertyRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<RentalProperty> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public RentalProperty get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Property not found: " + id));
    }

    @PostMapping
    public RentalProperty create(@Valid @RequestBody RentalProperty property) {
        property.setId(null);
        return repository.save(property);
    }

    @PutMapping("/{id}")
    public RentalProperty update(@PathVariable Long id, @Valid @RequestBody RentalProperty property) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Property not found: " + id);
        }
        property.setId(id);
        return repository.save(property);
    }
}
