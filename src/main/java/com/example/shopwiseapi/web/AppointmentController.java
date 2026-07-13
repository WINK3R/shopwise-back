package com.example.shopwiseapi.web;

import com.example.shopwiseapi.appointment.AppointmentRequest;
import com.example.shopwiseapi.appointment.AppointmentResponse;
import com.example.shopwiseapi.appointment.AppointmentStatus;
import com.example.shopwiseapi.appointment.AppointmentStatusRequest;
import com.example.shopwiseapi.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public List<AppointmentResponse> findAll(
            @RequestParam Long businessId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) Long clientId
    ) {
        return appointmentService.findAll(businessId, date, status, clientId);
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@Valid @RequestBody AppointmentRequest request) {
        return appointmentService.create(request);
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request
    ) {
        return appointmentService.updateStatus(id, request.status());
    }
}
