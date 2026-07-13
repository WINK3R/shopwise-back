package com.example.shopwiseapi.client;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        Long businessId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getBusiness() == null ? null : client.getBusiness().getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getActive(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
