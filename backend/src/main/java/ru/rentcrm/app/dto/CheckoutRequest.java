package ru.rentcrm.app.dto;

import ru.rentcrm.app.model.Tariff;

public record CheckoutRequest(Tariff tariff) {
}
