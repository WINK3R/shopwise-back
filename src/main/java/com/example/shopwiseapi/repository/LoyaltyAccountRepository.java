package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.client.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

    Optional<LoyaltyAccount> findByClientId(Long clientId);

    boolean existsByClientId(Long clientId);
}
