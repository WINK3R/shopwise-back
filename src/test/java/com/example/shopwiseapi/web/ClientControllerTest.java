package com.example.shopwiseapi.web;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
    }

    @Test
    void shouldFindAllClients() throws Exception {
        clientRepository.save(Client.builder()
                .prenom("Marie")
                .nom("Dupont")
                .email("marie.dupont@example.com")
                .telephone("0601020304")
                .build());
        clientRepository.save(Client.builder()
                .prenom("Paul")
                .nom("Martin")
                .email("paul.martin@example.com")
                .telephone("0611223344")
                .build());

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].prenom").value("Marie"))
                .andExpect(jsonPath("$[0].nom").value("Dupont"))
                .andExpect(jsonPath("$[0].actif").value(true))
                .andExpect(jsonPath("$[0].dateCreation", notNullValue()))
                .andExpect(jsonPath("$[0].dateModification", notNullValue()))
                .andExpect(jsonPath("$[1].prenom").value("Paul"))
                .andExpect(jsonPath("$[1].nom").value("Martin"));
    }

    @Test
    void shouldFindClientById() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .prenom("Marie")
                .nom("Dupont")
                .email("marie.dupont@example.com")
                .telephone("0601020304")
                .actif(false)
                .build());

        mockMvc.perform(get("/api/clients/{id}", client.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.prenom").value("Marie"))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.email").value("marie.dupont@example.com"))
                .andExpect(jsonPath("$.telephone").value("0601020304"))
                .andExpect(jsonPath("$.actif").value(false))
                .andExpect(jsonPath("$.dateCreation", notNullValue()))
                .andExpect(jsonPath("$.dateModification", notNullValue()));
    }

    @Test
    void shouldReturnNotFoundWhenFindingUnknownClient() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client introuvable avec l'id 999"));
    }

    @Test
    void shouldCreateClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prenom": "Marie",
                                  "nom": "Dupont",
                                  "email": "marie.dupont@example.com",
                                  "telephone": "0601020304",
                                  "actif": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.prenom").value("Marie"))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.email").value("marie.dupont@example.com"))
                .andExpect(jsonPath("$.telephone").value("0601020304"))
                .andExpect(jsonPath("$.actif").value(true))
                .andExpect(jsonPath("$.dateCreation", notNullValue()))
                .andExpect(jsonPath("$.dateModification", notNullValue()));
    }

    @Test
    void shouldRejectInvalidClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prenom": "",
                                  "nom": "",
                                  "email": "email-invalide",
                                  "telephone": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateClient() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .prenom("Marie")
                .nom("Dupont")
                .email("marie.dupont@example.com")
                .telephone("0601020304")
                .build());

        mockMvc.perform(put("/api/clients/{id}", client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prenom": "Marie",
                                  "nom": "Martin",
                                  "email": "marie.martin@example.com",
                                  "telephone": "0611223344",
                                  "actif": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.prenom").value("Marie"))
                .andExpect(jsonPath("$.nom").value("Martin"))
                .andExpect(jsonPath("$.email").value("marie.martin@example.com"))
                .andExpect(jsonPath("$.telephone").value("0611223344"))
                .andExpect(jsonPath("$.actif").value(false))
                .andExpect(jsonPath("$.dateCreation", notNullValue()))
                .andExpect(jsonPath("$.dateModification", notNullValue()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownClient() throws Exception {
        mockMvc.perform(put("/api/clients/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prenom": "Marie",
                                  "nom": "Martin",
                                  "email": "marie.martin@example.com",
                                  "telephone": "0611223344",
                                  "actif": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client introuvable avec l'id 999"));
    }
}
