package com.example.shopwiseapi.business;

public record MerchantResponse(
        Long id,
        Long businessId,
        String firstName,
        String lastName,
        String email,
        String role,
        Boolean active
) {
    public static MerchantResponse from(Merchant merchant) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getBusiness().getId(),
                merchant.getFirstName(),
                merchant.getLastName(),
                merchant.getEmail(),
                merchant.getRole(),
                merchant.getActive()
        );
    }
}
