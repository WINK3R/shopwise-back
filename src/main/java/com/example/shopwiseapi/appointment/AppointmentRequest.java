package com.example.shopwiseapi.appointment;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull Long businessId,
        @NotNull Long clientId,
        @NotNull Long serviceId,
        @NotNull LocalDateTime startsAt,
        String comment
) {
}
