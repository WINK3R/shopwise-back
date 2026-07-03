package com.example.shopwiseapi.client;

import jakarta.validation.constraints.NotBlank;

public record CustomerAccountRequest(
        @NotBlank String passwordHash,
        Boolean active
) {
}
