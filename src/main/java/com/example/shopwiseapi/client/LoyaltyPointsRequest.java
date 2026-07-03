package com.example.shopwiseapi.client;

import jakarta.validation.constraints.Positive;

public record LoyaltyPointsRequest(
        @Positive Integer points
) {
}
