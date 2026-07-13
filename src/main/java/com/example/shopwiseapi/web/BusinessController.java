package com.example.shopwiseapi.web;

import com.example.shopwiseapi.business.BusinessRequest;
import com.example.shopwiseapi.business.BusinessResponse;
import com.example.shopwiseapi.service.BusinessService;
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

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping
    public List<BusinessResponse> findAll() {
        return businessService.findAll();
    }

    @GetMapping("/{id}")
    public BusinessResponse findById(@PathVariable Long id) {
        return businessService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessResponse create(@Valid @RequestBody BusinessRequest request) {
        return businessService.create(request);
    }

    @PutMapping("/{id}")
    public BusinessResponse update(@PathVariable Long id, @Valid @RequestBody BusinessRequest request) {
        return businessService.update(id, request);
    }
}
