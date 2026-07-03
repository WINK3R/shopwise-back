package com.example.shopwiseapi.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String phone,
        Boolean active
) {
}
