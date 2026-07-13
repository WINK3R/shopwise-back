package com.example.shopwiseapi.appointment;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long businessId,
        Long clientId,
        Long serviceId,
        Long createdByAccountId,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        AppointmentStatus status,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getBusiness().getId(),
                appointment.getClient().getId(),
                appointment.getService().getId(),
                appointment.getCreatedBy() == null ? null : appointment.getCreatedBy().getId(),
                appointment.getStartsAt(),
                appointment.getEndsAt(),
                appointment.getStatus(),
                appointment.getComment(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
