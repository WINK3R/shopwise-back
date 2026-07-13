package com.example.shopwiseapi.service;

import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.business.Merchant;
import com.example.shopwiseapi.business.MerchantRequest;
import com.example.shopwiseapi.business.MerchantResponse;
import com.example.shopwiseapi.repository.MerchantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.example.shopwiseapi.merchant.MembershipRole;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final BusinessService businessService;
    private final BusinessAccessService businessAccessService;

    @Transactional(readOnly = true)
    public List<MerchantResponse> findAll(Long businessId) {
        businessAccessService.requireMembership(businessId, MembershipRole.OWNER);
        return merchantRepository.findByBusinessIdOrderByLastNameAscFirstNameAsc(businessId)
                .stream().map(MerchantResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MerchantResponse findById(Long id) {
        Merchant merchant = findEntity(id);
        businessAccessService.requireMembership(merchant.getBusiness().getId(), MembershipRole.OWNER);
        return MerchantResponse.from(merchant);
    }

    @Transactional
    public MerchantResponse create(MerchantRequest request) {
        businessAccessService.requireMembership(request.businessId(), MembershipRole.OWNER);
        Merchant merchant = new Merchant();
        apply(merchant, request);
        return MerchantResponse.from(merchantRepository.save(merchant));
    }

    @Transactional
    public MerchantResponse update(Long id, MerchantRequest request) {
        Merchant merchant = findEntity(id);
        businessAccessService.requireMembership(merchant.getBusiness().getId(), MembershipRole.OWNER);
        businessAccessService.requireMembership(request.businessId(), MembershipRole.OWNER);
        businessAccessService.requireSameBusiness(request.businessId(), merchant.getBusiness().getId());
        apply(merchant, request);
        return MerchantResponse.from(merchant);
    }

    Merchant findEntity(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found with id " + id));
    }

    private void apply(Merchant merchant, MerchantRequest request) {
        Business business = businessService.findEntity(request.businessId());
        merchant.setBusiness(business);
        merchant.setFirstName(request.firstName());
        merchant.setLastName(request.lastName());
        merchant.setEmail(request.email());
        merchant.setRole(request.role());
        merchant.setActive(request.active() == null || request.active());
    }
}
