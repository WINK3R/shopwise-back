package com.example.shopwiseapi.service;

import com.example.shopwiseapi.merchant.BusinessMembershipResponse;
import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.merchant.MerchantAccountResponse;
import com.example.shopwiseapi.merchant.MerchantSessionResponse;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import com.example.shopwiseapi.repository.MerchantAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantAuthService {

    private final MerchantAccountRepository accountRepository;
    private final BusinessMembershipRepository membershipRepository;
    private final BusinessAccessService businessAccessService;

    @Transactional
    public MerchantSessionResponse recordLoginAndGetSession(String email) {
        MerchantAccount account = accountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Merchant account not found"));
        account.setLastLogin(LocalDateTime.now());
        return toSession(account);
    }

    @Transactional(readOnly = true)
    public MerchantSessionResponse getCurrentSession() {
        return toSession(businessAccessService.currentAccount());
    }

    @Transactional(readOnly = true)
    public List<BusinessMembershipResponse> getCurrentBusinesses() {
        MerchantAccount account = businessAccessService.currentAccount();
        return membershipRepository.findByMerchantAccountIdAndActiveTrueOrderByBusinessName(account.getId())
                .stream()
                .map(BusinessMembershipResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MerchantSessionResponse toSession(MerchantAccount account) {
        List<BusinessMembershipResponse> businesses = membershipRepository
                .findByMerchantAccountIdAndActiveTrueOrderByBusinessName(account.getId())
                .stream()
                .map(BusinessMembershipResponse::from)
                .toList();
        return new MerchantSessionResponse(MerchantAccountResponse.from(account), businesses);
    }
}
