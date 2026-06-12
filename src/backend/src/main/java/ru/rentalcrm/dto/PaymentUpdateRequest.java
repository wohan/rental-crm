package ru.rentalcrm.dto;

import jakarta.validation.constraints.NotNull;
import ru.rentalcrm.model.PaymentMethod;

public record PaymentUpdateRequest(@NotNull PaymentMethod method, String comment) {
}
