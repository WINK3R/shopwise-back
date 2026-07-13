package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.merchant.MerchantAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantAccountRepository extends JpaRepository<MerchantAccount, Long> {

    Optional<MerchantAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
