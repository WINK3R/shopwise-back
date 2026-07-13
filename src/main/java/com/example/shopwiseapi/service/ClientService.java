package com.example.shopwiseapi.service;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.ClientRequest;
import com.example.shopwiseapi.client.ClientResponse;
import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.repository.BusinessRepository;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final BusinessRepository businessRepository;
    private final BusinessAccessService businessAccessService;

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll(Long businessId) {
        businessAccessService.requireMembership(businessId, MembershipRole.values());
        return clientRepository.findByBusinessIdOrderByLastNameAscFirstNameAsc(businessId)
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = findEntity(id);
        businessAccessService.requireMembership(client.getBusiness().getId(), MembershipRole.values());
        return ClientResponse.from(client);
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        businessAccessService.requireMembership(request.businessId(), MembershipRole.values());
        ensureEmailAvailable(request.email(), null);
        Client client = Client.builder()
                .business(findBusiness(request.businessId()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .active(resolveActive(request))
                .build();

        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findEntity(id);
        businessAccessService.requireMembership(client.getBusiness().getId(), MembershipRole.values());
        businessAccessService.requireMembership(request.businessId(), MembershipRole.values());
        businessAccessService.requireSameBusiness(request.businessId(), client.getBusiness().getId());

        ensureEmailAvailable(request.email(), id);

        client.setBusiness(findBusiness(request.businessId()));
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setActive(resolveActive(request));

        return ClientResponse.from(client);
    }

    private Boolean resolveActive(ClientRequest request) {
        return request.active() != null ? request.active() : true;
    }

    private Business findBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + businessId));
    }

    Client findEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + id));
    }

    private void ensureEmailAvailable(String email, Long clientId) {
        boolean exists = clientId == null
                ? clientRepository.existsByEmail(email)
                : clientRepository.existsByEmailAndIdNot(email, clientId);
        if (exists) {
            throw new ResourceAlreadyExistsException("Client already exists with email " + email);
        }
    }
}
