package com.example.shopwiseapi.merchant;

import java.time.LocalDateTime;

public record MerchantInvitationResponse(
        Long id,
        Long businessId,
        String businessName,
        String email,
        MembershipRole role,
        InvitationStatus status,
        String token,
        LocalDateTime expiresAt,
        Long createdById,
        LocalDateTime createdAt
) {
    public static MerchantInvitationResponse from(MerchantInvitation invitation) {
        return new MerchantInvitationResponse(
                invitation.getId(),
                invitation.getBusiness().getId(),
                invitation.getBusiness().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getToken(),
                invitation.getExpiresAt(),
                invitation.getCreatedBy().getId(),
                invitation.getCreatedAt()
        );
    }
}
