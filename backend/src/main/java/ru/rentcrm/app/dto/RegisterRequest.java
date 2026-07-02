package ru.rentcrm.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Email String email,
    @Size(min = 8) String password,
    @NotBlank String fullName,
    @NotBlank String accountName,
    Boolean termsAccepted,
    Boolean privacyAccepted,
    Boolean notificationConsent
) {
}
