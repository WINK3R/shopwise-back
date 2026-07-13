package com.example.shopwiseapi.service;

import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.catalog.Service;
import com.example.shopwiseapi.repository.CatalogServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BusinessDefaultsService {

    private static final List<DefaultService> DEFAULT_SERVICES = List.of(
            new DefaultService("Conseil personnalis\u00e9", 45, 25),
            new DefaultService("Retrait de commande", 30, 10),
            new DefaultService("Atelier d\u00e9couverte", 60, 40)
    );

    private final CatalogServiceRepository serviceRepository;

    public void createDefaultServices(Business business) {
        DEFAULT_SERVICES.forEach(defaultService -> createIfMissing(business, defaultService));
    }

    private void createIfMissing(Business business, DefaultService defaultService) {
        if (serviceRepository.existsByBusinessIdAndNameIgnoreCase(business.getId(), defaultService.name())) {
            return;
        }
        serviceRepository.save(Service.builder()
                .business(business)
                .name(defaultService.name())
                .description(defaultService.name())
                .durationMinutes(defaultService.durationMinutes())
                .loyaltyPoints(defaultService.loyaltyPoints())
                .build());
    }

    private record DefaultService(String name, int durationMinutes, int loyaltyPoints) {
    }
}
