package com.example.shopwiseapi.web;

import com.example.shopwiseapi.merchant.BusinessMembershipResponse;
import com.example.shopwiseapi.merchant.BusinessMembershipUpdateRequest;
import com.example.shopwiseapi.merchant.MerchantInvitationRequest;
import com.example.shopwiseapi.merchant.MerchantInvitationResponse;
import com.example.shopwiseapi.service.BusinessMembershipService;
import com.example.shopwiseapi.service.MerchantInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}")
@RequiredArgsConstructor
public class BusinessTeamController {

    private final BusinessMembershipService membershipService;
    private final MerchantInvitationService invitationService;

    @GetMapping("/members")
    public List<BusinessMembershipResponse> findMembers(@PathVariable Long businessId) {
        return membershipService.findByBusiness(businessId);
    }

    @PatchMapping("/members/{membershipId}")
    public BusinessMembershipResponse updateMembership(
            @PathVariable Long businessId,
            @PathVariable Long membershipId,
            @Valid @RequestBody BusinessMembershipUpdateRequest request
    ) {
        return membershipService.update(businessId, membershipId, request);
    }

    @PostMapping("/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantInvitationResponse createInvitation(
            @PathVariable Long businessId,
            @Valid @RequestBody MerchantInvitationRequest request
    ) {
        return invitationService.create(businessId, request);
    }

    @GetMapping("/invitations")
    public List<MerchantInvitationResponse> findInvitations(@PathVariable Long businessId) {
        return invitationService.findByBusiness(businessId);
    }
}
