package com.example.shopwiseapi.web;

import com.example.shopwiseapi.catalog.ServiceRequest;
import com.example.shopwiseapi.catalog.ServiceResponse;
import com.example.shopwiseapi.service.CatalogService;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class CatalogServiceController {

    private final CatalogService catalogService;

    @GetMapping
    public List<ServiceResponse> findAll(@RequestParam Long businessId) {
        return catalogService.findAll(businessId);
    }

    @GetMapping("/{id}")
    public ServiceResponse findById(@PathVariable Long id) {
        return catalogService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@Valid @RequestBody ServiceRequest request) {
        return catalogService.create(request);
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return catalogService.update(id, request);
    }
}
