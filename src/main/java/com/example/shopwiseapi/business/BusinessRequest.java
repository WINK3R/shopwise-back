package com.example.shopwiseapi.business;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BusinessRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String phone,
        Boolean active
) {
}
