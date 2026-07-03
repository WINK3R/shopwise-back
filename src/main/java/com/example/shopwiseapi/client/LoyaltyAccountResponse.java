package com.example.shopwiseapi.client;

import java.time.LocalDateTime;

public record LoyaltyAccountResponse(
        Long id,
        Long clientId,
        Integer pointsBalance,
        LocalDateTime updatedAt
) {

    public static LoyaltyAccountResponse from(LoyaltyAccount loyaltyAccount) {
        return new LoyaltyAccountResponse(
                loyaltyAccount.getId(),
                loyaltyAccount.getClient().getId(),
                loyaltyAccount.getPointsBalance(),
                loyaltyAccount.getUpdatedAt()
        );
    }
}
