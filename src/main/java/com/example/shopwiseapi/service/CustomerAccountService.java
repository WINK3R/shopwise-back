package com.example.shopwiseapi.service;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.CustomerAccount;
import com.example.shopwiseapi.client.CustomerAccountRequest;
import com.example.shopwiseapi.client.CustomerAccountResponse;
import com.example.shopwiseapi.client.CustomerLoginRequest;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.CustomerAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.example.shopwiseapi.merchant.MembershipRole;

@Service
@RequiredArgsConstructor
public class CustomerAccountService {

    private final ClientRepository clientRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final BusinessAccessService businessAccessService;

    @Transactional(readOnly = true)
    public CustomerAccountResponse findByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + clientId));
        businessAccessService.requireMembership(client.getBusiness().getId(), MembershipRole.values());
        return customerAccountRepository.findByClientId(clientId)
                .map(CustomerAccountResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Customer account not found for client " + clientId));
    }

    @Transactional
    public CustomerAccountResponse create(Long clientId, CustomerAccountRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + clientId));
        businessAccessService.requireMembership(
                client.getBusiness().getId(),
                MembershipRole.OWNER,
                MembershipRole.MANAGER
        );

        if (customerAccountRepository.existsByClientId(clientId)) {
            throw new ResourceAlreadyExistsException("Customer account already exists for client " + clientId);
        }

        CustomerAccount customerAccount = CustomerAccount.builder()
                .client(client)
                .passwordHash(request.passwordHash())
                .active(resolveActive(request))
                .build();

        return CustomerAccountResponse.from(customerAccountRepository.save(customerAccount));
    }

    @Transactional
    public CustomerAccountResponse login(CustomerLoginRequest request) {
        CustomerAccount customerAccount = customerAccountRepository.findByClientEmail(request.email())
                .filter(account -> account.getActive() && account.getPasswordHash().equals(request.passwordHash()))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid customer credentials"));

        customerAccount.setLastLogin(LocalDateTime.now());
        return CustomerAccountResponse.from(customerAccount);
    }

    private Boolean resolveActive(CustomerAccountRequest request) {
        return request.active() != null ? request.active() : true;
    }
}
