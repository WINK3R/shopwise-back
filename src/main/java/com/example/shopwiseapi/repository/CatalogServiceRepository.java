package com.example.shopwiseapi.repository;

import com.example.shopwiseapi.catalog.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByBusinessIdOrderByName(Long businessId);

    boolean existsByBusinessIdAndNameIgnoreCase(Long businessId, String name);
}
