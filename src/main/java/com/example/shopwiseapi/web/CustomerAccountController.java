package com.example.shopwiseapi.web;

import com.example.shopwiseapi.client.CustomerAccountRequest;
import com.example.shopwiseapi.client.CustomerAccountResponse;
import com.example.shopwiseapi.service.CustomerAccountService;
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

@RestController
@RequestMapping("/api/clients/{clientId}/account")
@RequiredArgsConstructor
public class CustomerAccountController {

    private final CustomerAccountService customerAccountService;

    @GetMapping
    public CustomerAccountResponse findByClientId(@PathVariable Long clientId) {
        return customerAccountService.findByClientId(clientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerAccountResponse create(
            @PathVariable Long clientId,
            @Valid @RequestBody CustomerAccountRequest request
    ) {
        return customerAccountService.create(clientId, request);
    }
}
