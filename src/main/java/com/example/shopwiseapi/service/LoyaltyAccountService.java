package com.example.shopwiseapi.service;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.LoyaltyAccount;
import com.example.shopwiseapi.client.LoyaltyAccountRequest;
import com.example.shopwiseapi.client.LoyaltyAccountResponse;
import com.example.shopwiseapi.client.LoyaltyPointsRequest;
import com.example.shopwiseapi.client.LoyaltyTransaction;
import com.example.shopwiseapi.client.LoyaltyTransactionResponse;
import com.example.shopwiseapi.client.TransactionType;
import com.example.shopwiseapi.appointment.Appointment;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.LoyaltyAccountRepository;
import com.example.shopwiseapi.repository.LoyaltyTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.example.shopwiseapi.merchant.MembershipRole;

@Service
@RequiredArgsConstructor
public class LoyaltyAccountService {

    private final ClientRepository clientRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final BusinessAccessService businessAccessService;

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse findByClientId(Long clientId) {
        requireClientAccess(clientId, MembershipRole.values());
        return loyaltyAccountRepository.findByClientId(clientId)
                .map(LoyaltyAccountResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for client " + clientId));
    }

    @Transactional
    public LoyaltyAccountResponse create(Long clientId, LoyaltyAccountRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + clientId));
        businessAccessService.requireMembership(
                client.getBusiness().getId(),
                MembershipRole.OWNER,
                MembershipRole.MANAGER
        );

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
        requireClientAccess(clientId, MembershipRole.OWNER, MembershipRole.MANAGER);
        LoyaltyAccount loyaltyAccount = findLoyaltyAccount(clientId);

        loyaltyAccount.creditPoints(request.points());
        saveTransaction(loyaltyAccount, null, TransactionType.ADJUSTMENT, request.points(), "Manual credit");

        return LoyaltyAccountResponse.from(loyaltyAccount);
    }

    @Transactional
    public LoyaltyAccountResponse debit(Long clientId, LoyaltyPointsRequest request) {
        requireClientAccess(clientId, MembershipRole.OWNER, MembershipRole.MANAGER);
        LoyaltyAccount loyaltyAccount = findLoyaltyAccount(clientId);

        if (loyaltyAccount.getPointsBalance() < request.points()) {
            throw new InvalidOperationException("Insufficient loyalty points for client " + clientId);
        }
        loyaltyAccount.debitPoints(request.points());
        saveTransaction(loyaltyAccount, null, TransactionType.REDEEMED, -request.points(), "Manual debit");

        return LoyaltyAccountResponse.from(loyaltyAccount);
    }

    private LoyaltyAccount findLoyaltyAccount(Long clientId) {
        return loyaltyAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Loyalty account not found for client " + clientId));
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> findTransactions(Long clientId) {
        requireClientAccess(clientId, MembershipRole.values());
        return loyaltyTransactionRepository.findByLoyaltyAccountClientIdOrderByTransactionDateDesc(clientId)
                .stream()
                .map(LoyaltyTransactionResponse::from)
                .toList();
    }

    void creditForAppointment(Appointment appointment) {
        if (loyaltyTransactionRepository.existsByAppointmentId(appointment.getId())) {
            return;
        }

        LoyaltyAccount loyaltyAccount = loyaltyAccountRepository.findByClientId(appointment.getClient().getId())
                .orElseGet(() -> loyaltyAccountRepository.save(LoyaltyAccount.builder()
                        .client(appointment.getClient())
                        .build()));
        int points = appointment.getService().getLoyaltyPoints();
        loyaltyAccount.creditPoints(points);
        saveTransaction(
                loyaltyAccount,
                appointment,
                TransactionType.EARNED,
                points,
                "Appointment honored"
        );
    }

    private void saveTransaction(
            LoyaltyAccount loyaltyAccount,
            Appointment appointment,
            TransactionType type,
            Integer pointsDelta,
            String reason
    ) {
        loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
                .loyaltyAccount(loyaltyAccount)
                .appointment(appointment)
                .type(type)
                .pointsDelta(pointsDelta)
                .reason(reason)
                .transactionDate(LocalDateTime.now())
                .build());
    }

    private Integer resolvePointsBalance(LoyaltyAccountRequest request) {
        return request.pointsBalance() != null ? request.pointsBalance() : 0;
    }

    private void requireClientAccess(Long clientId, MembershipRole... roles) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + clientId));
        businessAccessService.requireMembership(client.getBusiness().getId(), roles);
    }
}
