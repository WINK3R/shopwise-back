package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.merchant.InvitationStatus;
import com.example.shopwiseapi.merchant.MerchantInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerchantInvitationRepository extends JpaRepository<MerchantInvitation, Long> {

    Optional<MerchantInvitation> findByToken(String token);

    Optional<MerchantInvitation> findByIdAndBusinessId(Long id, Long businessId);

    List<MerchantInvitation> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    boolean existsByBusinessIdAndEmailIgnoreCaseAndStatus(Long businessId, String email, InvitationStatus status);
}
