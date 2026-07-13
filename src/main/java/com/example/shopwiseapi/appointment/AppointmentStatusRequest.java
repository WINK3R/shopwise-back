package com.example.shopwiseapi.appointment;

import jakarta.validation.constraints.NotNull;

public record AppointmentStatusRequest(@NotNull AppointmentStatus status) {
}
