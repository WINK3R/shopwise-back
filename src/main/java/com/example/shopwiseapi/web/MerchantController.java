package com.example.shopwiseapi.web;

import com.example.shopwiseapi.business.MerchantRequest;
import com.example.shopwiseapi.business.MerchantResponse;
import com.example.shopwiseapi.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@Deprecated
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public List<MerchantResponse> findAll(@RequestParam Long businessId) {
        return merchantService.findAll(businessId);
    }

    @GetMapping("/{id}")
    public MerchantResponse findById(@PathVariable Long id) {
        return merchantService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantResponse create(@Valid @RequestBody MerchantRequest request) {
        return merchantService.create(request);
    }

    @PutMapping("/{id}")
    public MerchantResponse update(@PathVariable Long id, @Valid @RequestBody MerchantRequest request) {
        return merchantService.update(id, request);
    }
}
