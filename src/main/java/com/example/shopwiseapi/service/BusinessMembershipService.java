package com.example.shopwiseapi.service;

import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.BusinessMembershipResponse;
import com.example.shopwiseapi.merchant.BusinessMembershipUpdateRequest;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessMembershipService {

    private final BusinessMembershipRepository membershipRepository;
    private final BusinessAccessService businessAccessService;

    @Transactional(readOnly = true)
    public List<BusinessMembershipResponse> findByBusiness(Long businessId) {
        businessAccessService.requireMembership(businessId, MembershipRole.OWNER);
        return membershipRepository.findByBusinessIdOrderByMerchantAccountLastName(businessId)
                .stream()
                .map(BusinessMembershipResponse::from)
                .toList();
    }

    @Transactional
    public BusinessMembershipResponse update(
            Long businessId,
            Long membershipId,
            BusinessMembershipUpdateRequest request
    ) {
        businessAccessService.requireMembership(businessId, MembershipRole.OWNER);
        BusinessMembership membership = membershipRepository.findByIdAndBusinessId(membershipId, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Membership not found with id " + membershipId + " in business " + businessId
                ));

        boolean removesOwner = membership.getRole() == MembershipRole.OWNER
                && ((request.role() != null && request.role() != MembershipRole.OWNER)
                || Boolean.FALSE.equals(request.active()));
        if (removesOwner && membershipRepository.countByBusinessIdAndRoleAndActiveTrue(
                businessId,
                MembershipRole.OWNER
        ) <= 1) {
            throw new InvalidOperationException("A business must keep at least one active owner");
        }

        if (request.role() != null) {
            membership.setRole(request.role());
        }
        if (request.active() != null) {
            membership.setActive(request.active());
        }
        return BusinessMembershipResponse.from(membership);
    }
}
