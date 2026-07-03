package com.example.shopwiseapi.client;

import jakarta.validation.constraints.PositiveOrZero;

public record LoyaltyAccountRequest(
        @PositiveOrZero Integer pointsBalance
) {
}
