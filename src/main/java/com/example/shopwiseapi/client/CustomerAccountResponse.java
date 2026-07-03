package com.example.shopwiseapi.client;

import java.time.LocalDateTime;

public record CustomerAccountResponse(
        Long id,
        Long clientId,
        Boolean active,
        LocalDateTime lastLogin
) {

    public static CustomerAccountResponse from(CustomerAccount customerAccount) {
        return new CustomerAccountResponse(
                customerAccount.getId(),
                customerAccount.getClient().getId(),
                customerAccount.getActive(),
                customerAccount.getLastLogin()
        );
    }
}
