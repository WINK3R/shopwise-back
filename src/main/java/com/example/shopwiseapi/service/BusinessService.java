package com.example.shopwiseapi.service;

import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.business.BusinessRequest;
import com.example.shopwiseapi.business.BusinessResponse;
import com.example.shopwiseapi.repository.BusinessRepository;
import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMembershipRepository membershipRepository;
    private final BusinessAccessService businessAccessService;
    private final BusinessDefaultsService businessDefaultsService;

    @Transactional(readOnly = true)
    public List<BusinessResponse> findAll() {
        MerchantAccount account = businessAccessService.currentAccount();
        return membershipRepository.findByMerchantAccountIdAndActiveTrueOrderByBusinessName(account.getId())
                .stream()
                .map(BusinessMembership::getBusiness)
                .map(BusinessResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessResponse findById(Long id) {
        businessAccessService.requireAnyMembership(id);
        return BusinessResponse.from(findEntity(id));
    }

    @Transactional
    public BusinessResponse create(BusinessRequest request) {
        MerchantAccount creator = businessAccessService.requireOwnerOrAccountWithoutBusiness();
        Business business = Business.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .active(request.active() == null || request.active())
                .build();
        Business savedBusiness = businessRepository.save(business);
        membershipRepository.save(BusinessMembership.builder()
                .merchantAccount(creator)
                .business(savedBusiness)
                .role(MembershipRole.OWNER)
                .build());
        businessDefaultsService.createDefaultServices(savedBusiness);
        return BusinessResponse.from(savedBusiness);
    }

    @Transactional
    public BusinessResponse update(Long id, BusinessRequest request) {
        businessAccessService.requireMembership(id, MembershipRole.OWNER);
        Business business = findEntity(id);
        business.setName(request.name());
        business.setEmail(request.email());
        business.setPhone(request.phone());
        business.setActive(request.active() == null || request.active());
        return BusinessResponse.from(business);
    }

    Business findEntity(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
    }
}
