package com.example.shopwiseapi.service;

import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import com.example.shopwiseapi.repository.MerchantAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessAccessService {

    private final MerchantAccountRepository merchantAccountRepository;
    private final BusinessMembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public MerchantAccount currentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return merchantAccountRepository.findByEmailIgnoreCase(authentication.getName())
                .filter(MerchantAccount::getActive)
                .orElseThrow(() -> new AccessDeniedException("Merchant account is inactive"));
    }

    @Transactional(readOnly = true)
    public BusinessMembership requireMembership(Long businessId, MembershipRole... allowedRoles) {
        MerchantAccount account = currentAccount();
        BusinessMembership membership = membershipRepository
                .findByMerchantAccountIdAndBusinessId(account.getId(), businessId)
                .filter(BusinessMembership::getActive)
                .filter(value -> value.getBusiness().getActive())
                .orElseThrow(() -> new AccessDeniedException("Business access denied"));

        Set<MembershipRole> roles = Set.copyOf(Arrays.asList(allowedRoles));
        if (!roles.isEmpty() && !roles.contains(membership.getRole())) {
            throw new AccessDeniedException("Insufficient role for business " + businessId);
        }
        return membership;
    }

    @Transactional(readOnly = true)
    public BusinessMembership requireAnyMembership(Long businessId) {
        return requireMembership(businessId, MembershipRole.values());
    }

    @Transactional(readOnly = true)
    public MerchantAccount requireOwnerOrAccountWithoutBusiness() {
        MerchantAccount account = currentAccount();
        List<BusinessMembership> memberships = membershipRepository
                .findByMerchantAccountIdAndActiveTrueOrderByBusinessName(account.getId());
        if (!memberships.isEmpty() && memberships.stream().noneMatch(
                membership -> membership.getRole() == MembershipRole.OWNER
        )) {
            throw new AccessDeniedException("Only an owner can create another business");
        }
        return account;
    }

    public void requireSameBusiness(Long expectedBusinessId, Long resourceBusinessId) {
        if (!expectedBusinessId.equals(resourceBusinessId)) {
            throw new EntityNotFoundException("Resource not found in business " + expectedBusinessId);
        }
    }
}
