package com.example.shopwiseapi.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MerchantInvitationRequest(
        @NotBlank @Email String email,
        @NotNull MembershipRole role
) {
}
