package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.client.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {

    Optional<CustomerAccount> findByClientId(Long clientId);

    boolean existsByClientId(Long clientId);
}
