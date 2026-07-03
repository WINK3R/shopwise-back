package com.example.shopwiseapi.service;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.LoyaltyAccount;
import com.example.shopwiseapi.client.LoyaltyAccountRequest;
import com.example.shopwiseapi.client.LoyaltyAccountResponse;
import com.example.shopwiseapi.client.LoyaltyPointsRequest;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.LoyaltyAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoyaltyAccountService {

    private final ClientRepository clientRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse findByClientId(Long clientId) {
        return loyaltyAccountRepository.findByClientId(clientId)
                .map(LoyaltyAccountResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for client " + clientId));
    }

    @Transactional
    public LoyaltyAccountResponse create(Long clientId, LoyaltyAccountRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + clientId));

        if (loyaltyAccountRepository.existsByClientId(clientId)) {
            throw new ResourceAlreadyExistsException("Loyalty account already exists for client " + clientId);
        }

        LoyaltyAccount loyaltyAccount = LoyaltyAccount.builder()
                .client(client)
                .pointsBalance(resolvePointsBalance(request))
                .build();

        return LoyaltyAccountResponse.from(loyaltyAccountRepository.save(loyaltyAccount));
    }

    @Transactional
    public LoyaltyAccountResponse credit(Long clientId, LoyaltyPointsRequest request) {
        LoyaltyAccount loyaltyAccount = findLoyaltyAccount(clientId);

        loyaltyAccount.creditPoints(request.points());

        return LoyaltyAccountResponse.from(loyaltyAccount);
    }

    @Transactional
    public LoyaltyAccountResponse debit(Long clientId, LoyaltyPointsRequest request) {
        LoyaltyAccount loyaltyAccount = findLoyaltyAccount(clientId);

        loyaltyAccount.debitPoints(request.points());

        return LoyaltyAccountResponse.from(loyaltyAccount);
    }

    private LoyaltyAccount findLoyaltyAccount(Long clientId) {
        return loyaltyAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for client " + clientId));
    }

    private Integer resolvePointsBalance(LoyaltyAccountRequest request) {
        return request.pointsBalance() != null ? request.pointsBalance() : 0;
    }
}
