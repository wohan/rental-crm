package ru.rentcrm.app.dto;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
    @Email String email
) {
}
