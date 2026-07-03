package com.example.shopwiseapi.service;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.ClientRequest;
import com.example.shopwiseapi.client.ClientResponse;
import com.example.shopwiseapi.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll()
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return clientRepository.findById(id)
                .map(ClientResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec l'id " + id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = Client.builder()
                .prenom(request.prenom())
                .nom(request.nom())
                .email(request.email())
                .telephone(request.telephone())
                .actif(resolveActif(request))
                .build();

        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec l'id " + id));

        client.setPrenom(request.prenom());
        client.setNom(request.nom());
        client.setEmail(request.email());
        client.setTelephone(request.telephone());
        client.setActif(resolveActif(request));

        return ClientResponse.from(client);
    }

    private Boolean resolveActif(ClientRequest request) {
        return request.actif() != null ? request.actif() : true;
    }
}
