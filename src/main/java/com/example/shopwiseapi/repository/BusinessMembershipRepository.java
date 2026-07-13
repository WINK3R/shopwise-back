package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.merchant.BusinessMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessMembershipRepository extends JpaRepository<BusinessMembership, Long> {

    List<BusinessMembership> findByMerchantAccountIdAndActiveTrueOrderByBusinessName(Long accountId);

    List<BusinessMembership> findByBusinessIdOrderByMerchantAccountLastName(Long businessId);

    Optional<BusinessMembership> findByMerchantAccountIdAndBusinessId(Long accountId, Long businessId);

    Optional<BusinessMembership> findByIdAndBusinessId(Long id, Long businessId);

    boolean existsByMerchantAccountIdAndBusinessId(Long accountId, Long businessId);

    long countByBusinessIdAndRoleAndActiveTrue(Long businessId, com.example.shopwiseapi.merchant.MembershipRole role);
}
