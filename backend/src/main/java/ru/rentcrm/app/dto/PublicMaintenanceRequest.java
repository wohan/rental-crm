package ru.rentcrm.app.dto;

import jakarta.validation.constraints.NotBlank;

public record PublicMaintenanceRequest(
    @NotBlank String title,
    String description,
    String contactName,
    String contactPhone
) {
}
