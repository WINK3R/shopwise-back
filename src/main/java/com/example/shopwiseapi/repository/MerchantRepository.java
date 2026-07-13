package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.business.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    List<Merchant> findByBusinessIdOrderByLastNameAscFirstNameAsc(Long businessId);
}
