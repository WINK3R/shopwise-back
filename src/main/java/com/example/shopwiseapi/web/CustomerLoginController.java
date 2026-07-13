package com.example.shopwiseapi.web;

import com.example.shopwiseapi.client.CustomerAccountResponse;
import com.example.shopwiseapi.client.CustomerLoginRequest;
import com.example.shopwiseapi.service.CustomerAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-accounts")
@RequiredArgsConstructor
public class CustomerLoginController {

    private final CustomerAccountService customerAccountService;

    @PostMapping("/login")
    public CustomerAccountResponse login(@Valid @RequestBody CustomerLoginRequest request) {
        return customerAccountService.login(request);
    }
}
