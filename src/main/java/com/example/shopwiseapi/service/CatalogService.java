package com.example.shopwiseapi.service;

import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.catalog.Service;
import com.example.shopwiseapi.catalog.ServiceRequest;
import com.example.shopwiseapi.catalog.ServiceResponse;
import com.example.shopwiseapi.repository.CatalogServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.example.shopwiseapi.merchant.MembershipRole;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogServiceRepository serviceRepository;
    private final BusinessService businessService;
    private final BusinessAccessService businessAccessService;

    @Transactional(readOnly = true)
    public List<ServiceResponse> findAll(Long businessId) {
        businessAccessService.requireMembership(businessId, MembershipRole.values());
        return serviceRepository.findByBusinessIdOrderByName(businessId)
                .stream().map(ServiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(Long id) {
        Service service = findEntity(id);
        businessAccessService.requireMembership(service.getBusiness().getId(), MembershipRole.values());
        return ServiceResponse.from(service);
    }

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        businessAccessService.requireMembership(
                request.businessId(),
                MembershipRole.OWNER,
                MembershipRole.MANAGER
        );
        Service service = new Service();
        apply(service, request);
        return ServiceResponse.from(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceRequest request) {
        Service service = findEntity(id);
        businessAccessService.requireMembership(
                service.getBusiness().getId(),
                MembershipRole.OWNER,
                MembershipRole.MANAGER
        );
        businessAccessService.requireMembership(
                request.businessId(),
                MembershipRole.OWNER,
                MembershipRole.MANAGER
        );
        businessAccessService.requireSameBusiness(request.businessId(), service.getBusiness().getId());
        apply(service, request);
        return ServiceResponse.from(service);
    }

    Service findEntity(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found with id " + id));
    }

    private void apply(Service service, ServiceRequest request) {
        Business business = businessService.findEntity(request.businessId());
        service.setBusiness(business);
        service.setName(request.name());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setLoyaltyPoints(request.loyaltyPoints());
        service.setActive(request.active() == null || request.active());
    }
}
