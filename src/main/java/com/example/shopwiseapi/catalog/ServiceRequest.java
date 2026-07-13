package com.example.shopwiseapi.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ServiceRequest(
        @NotNull Long businessId,
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Positive Integer durationMinutes,
        @NotNull @PositiveOrZero Integer loyaltyPoints,
        Boolean active
) {
}
