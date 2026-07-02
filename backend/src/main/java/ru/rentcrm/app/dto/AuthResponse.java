package ru.rentcrm.app.dto;

import java.util.UUID;

public record AuthResponse(
    String token,
    UUID accountId,
    String fullName,
    String role,
    Boolean emailVerified,
    String verificationLink
) {
}
