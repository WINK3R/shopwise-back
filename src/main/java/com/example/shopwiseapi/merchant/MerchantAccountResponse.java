package com.example.shopwiseapi.merchant;

import java.time.LocalDateTime;

public record MerchantAccountResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Boolean active,
        LocalDateTime lastLogin
) {
    public static MerchantAccountResponse from(MerchantAccount account) {
        return new MerchantAccountResponse(
                account.getId(),
                account.getFirstName(),
                account.getLastName(),
                account.getEmail(),
                account.getActive(),
                account.getLastLogin()
        );
    }
}
