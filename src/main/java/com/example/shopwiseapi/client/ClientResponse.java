package com.example.shopwiseapi.client;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String prenom,
        String nom,
        String email,
        String telephone,
        Boolean actif,
        LocalDateTime dateCreation,
        LocalDateTime dateModification
) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getPrenom(),
                client.getNom(),
                client.getEmail(),
                client.getTelephone(),
                client.getActif(),
                client.getDateCreation(),
                client.getDateModification()
        );
    }
}
