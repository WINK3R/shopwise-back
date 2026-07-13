package com.example.shopwiseapi.web;

import com.example.shopwiseapi.client.LoyaltyAccountRequest;
import com.example.shopwiseapi.client.LoyaltyAccountResponse;
import com.example.shopwiseapi.client.LoyaltyPointsRequest;
import com.example.shopwiseapi.client.LoyaltyTransactionResponse;
import com.example.shopwiseapi.service.LoyaltyAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/loyalty")
@RequiredArgsConstructor
public class LoyaltyAccountController {

    private final LoyaltyAccountService loyaltyAccountService;

    @GetMapping
    public LoyaltyAccountResponse findByClientId(@PathVariable Long clientId) {
        return loyaltyAccountService.findByClientId(clientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoyaltyAccountResponse create(
            @PathVariable Long clientId,
            @Valid @RequestBody LoyaltyAccountRequest request
    ) {
        return loyaltyAccountService.create(clientId, request);
    }

    @PostMapping("/credit")
    public LoyaltyAccountResponse credit(
            @PathVariable Long clientId,
            @Valid @RequestBody LoyaltyPointsRequest request
    ) {
        return loyaltyAccountService.credit(clientId, request);
    }

    @PostMapping("/debit")
    public LoyaltyAccountResponse debit(
            @PathVariable Long clientId,
            @Valid @RequestBody LoyaltyPointsRequest request
    ) {
        return loyaltyAccountService.debit(clientId, request);
    }

    @GetMapping("/transactions")
    public List<LoyaltyTransactionResponse> findTransactions(@PathVariable Long clientId) {
        return loyaltyAccountService.findTransactions(clientId);
    }
}
