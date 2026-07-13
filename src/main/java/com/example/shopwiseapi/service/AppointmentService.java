package com.example.shopwiseapi.service;

import com.example.shopwiseapi.appointment.Appointment;
import com.example.shopwiseapi.appointment.AppointmentRequest;
import com.example.shopwiseapi.appointment.AppointmentResponse;
import com.example.shopwiseapi.appointment.AppointmentStatus;
import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.catalog.Service;
import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.repository.AppointmentRepository;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.MembershipRole;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final BusinessService businessService;
    private final CatalogService catalogService;
    private final LoyaltyAccountService loyaltyAccountService;
    private final BusinessAccessService businessAccessService;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll(
            Long businessId,
            LocalDate date,
            AppointmentStatus status,
            Long clientId
    ) {
        businessAccessService.requireMembership(businessId, MembershipRole.values());
        Specification<Appointment> filters = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (businessId != null) {
                predicates.add(builder.equal(root.get("business").get("id"), businessId));
            }
            if (date != null) {
                LocalDateTime start = date.atStartOfDay();
                predicates.add(builder.greaterThanOrEqualTo(root.get("startsAt"), start));
                predicates.add(builder.lessThan(root.get("startsAt"), start.plusDays(1)));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (clientId != null) {
                predicates.add(builder.equal(root.get("client").get("id"), clientId));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };

        return appointmentRepository.findAll(filters, Sort.by("startsAt"))
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        Appointment appointment = findEntity(id);
        businessAccessService.requireMembership(appointment.getBusiness().getId(), MembershipRole.values());
        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        BusinessMembership membership = businessAccessService.requireMembership(
                request.businessId(),
                MembershipRole.values()
        );
        Business business = businessService.findEntity(request.businessId());
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + request.clientId()));
        Service service = catalogService.findEntity(request.serviceId());
        ensureSameBusiness(business, client, service);

        Appointment appointment = Appointment.builder()
                .business(business)
                .client(client)
                .service(service)
                .createdBy(membership.getMerchantAccount())
                .startsAt(request.startsAt())
                .endsAt(request.startsAt().plusMinutes(service.getDurationMinutes()))
                .comment(request.comment())
                .build();
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus requestedStatus) {
        Appointment appointment = findEntity(id);
        businessAccessService.requireMembership(appointment.getBusiness().getId(), MembershipRole.values());
        AppointmentStatus currentStatus = appointment.getStatus();

        if (requestedStatus == AppointmentStatus.SCHEDULED && currentStatus != AppointmentStatus.SCHEDULED) {
            throw new InvalidOperationException("A completed appointment cannot be rescheduled");
        }
        if (currentStatus != AppointmentStatus.SCHEDULED && currentStatus != requestedStatus) {
            throw new InvalidOperationException(
                    "Appointment status cannot change from " + currentStatus + " to " + requestedStatus
            );
        }

        if (requestedStatus == AppointmentStatus.HONORED) {
            appointment.markAsHonored();
            appointmentRepository.flush();
            loyaltyAccountService.creditForAppointment(appointment);
        } else if (requestedStatus == AppointmentStatus.CANCELED) {
            appointment.cancel();
        }

        return AppointmentResponse.from(appointment);
    }

    private Appointment findEntity(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id " + id));
    }

    private void ensureSameBusiness(Business business, Client client, Service service) {
        if (!business.getId().equals(client.getBusiness().getId())) {
            throw new InvalidOperationException("Client does not belong to business " + business.getId());
        }
        if (!business.getId().equals(service.getBusiness().getId())) {
            throw new InvalidOperationException("Service does not belong to business " + business.getId());
        }
    }
}
