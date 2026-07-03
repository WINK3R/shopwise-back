package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}

