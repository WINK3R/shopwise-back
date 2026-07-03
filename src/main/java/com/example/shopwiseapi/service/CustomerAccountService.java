package com.example.shopwiseapi.service;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.CustomerAccount;
import com.example.shopwiseapi.client.CustomerAccountRequest;
import com.example.shopwiseapi.client.CustomerAccountResponse;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.CustomerAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerAccountService {

    private final ClientRepository clientRepository;
    private final CustomerAccountRepository customerAccountRepository;

    @Transactional(readOnly = true)
    public CustomerAccountResponse findByClientId(Long clientId) {
        return customerAccountRepository.findByClientId(clientId)
                .map(CustomerAccountResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Customer account not found for client " + clientId));
    }

    @Transactional
    public CustomerAccountResponse create(Long clientId, CustomerAccountRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + clientId));

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

    private Boolean resolveActive(CustomerAccountRequest request) {
        return request.active() != null ? request.active() : true;
    }
}
