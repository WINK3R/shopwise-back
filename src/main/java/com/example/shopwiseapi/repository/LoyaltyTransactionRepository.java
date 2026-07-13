package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.client.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    List<LoyaltyTransaction> findByLoyaltyAccountClientIdOrderByTransactionDateDesc(Long clientId);

    boolean existsByAppointmentId(Long appointmentId);
}
