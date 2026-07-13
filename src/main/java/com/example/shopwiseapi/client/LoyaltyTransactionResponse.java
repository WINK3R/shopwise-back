package com.example.shopwiseapi.client;

import java.time.LocalDateTime;

public record LoyaltyTransactionResponse(
        Long id,
        Long appointmentId,
        TransactionType type,
        Integer pointsDelta,
        String reason,
        LocalDateTime transactionDate
) {
    public static LoyaltyTransactionResponse from(LoyaltyTransaction transaction) {
        return new LoyaltyTransactionResponse(
                transaction.getId(),
                transaction.getAppointment() == null ? null : transaction.getAppointment().getId(),
                transaction.getType(),
                transaction.getPointsDelta(),
                transaction.getReason(),
                transaction.getTransactionDate()
        );
    }
}
