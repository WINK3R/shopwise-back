package com.example.shopwiseapi.merchant;

public record BusinessMembershipResponse(
        Long id,
        Long businessId,
        String businessName,
        Long accountId,
        String firstName,
        String lastName,
        String email,
        MembershipRole role,
        Boolean active
) {
    public static BusinessMembershipResponse from(BusinessMembership membership) {
        return new BusinessMembershipResponse(
                membership.getId(),
                membership.getBusiness().getId(),
                membership.getBusiness().getName(),
                membership.getMerchantAccount().getId(),
                membership.getMerchantAccount().getFirstName(),
                membership.getMerchantAccount().getLastName(),
                membership.getMerchantAccount().getEmail(),
                membership.getRole(),
                membership.getActive()
        );
    }
}
