package com.example.shopwiseapi.merchant;

public record MerchantInvitationAcceptRequest(
        String firstName,
        String lastName,
        String password
) {
}
