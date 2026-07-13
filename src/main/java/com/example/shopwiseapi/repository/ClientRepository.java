package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Client> findByBusinessIdOrderByLastNameAscFirstNameAsc(Long businessId);
}
