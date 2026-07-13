package com.example.shopwiseapi.web;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.CustomerAccountRepository;
import com.example.shopwiseapi.repository.LoyaltyAccountRepository;
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
class ClientControllerTest extends AbstractMerchantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Test
    void shouldFindAllClients() throws Exception {
        clientRepository.save(Client.builder()
                .business(business)
                .firstName("Marie")
                .lastName("Dupont")
                .email("marie.dupont@example.com")
                .phone("0601020304")
                .build());
        clientRepository.save(Client.builder()
                .business(business)
                .firstName("Paul")
                .lastName("Martin")
                .email("paul.martin@example.com")
                .phone("0611223344")
                .build());

        mockMvc.perform(get("/api/clients").param("businessId", business.getId().toString()).with(merchant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Marie"))
                .andExpect(jsonPath("$[0].lastName").value("Dupont"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$[0].updatedAt", notNullValue()))
                .andExpect(jsonPath("$[1].firstName").value("Paul"))
                .andExpect(jsonPath("$[1].lastName").value("Martin"));
    }

    @Test
    void shouldFindClientById() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .business(business)
                .firstName("Marie")
                .lastName("Dupont")
                .email("marie.dupont@example.com")
                .phone("0601020304")
                .active(false)
                .build());

        mockMvc.perform(get("/api/clients/{id}", client.getId()).with(merchant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.firstName").value("Marie"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.email").value("marie.dupont@example.com"))
                .andExpect(jsonPath("$.phone").value("0601020304"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void shouldReturnNotFoundWhenFindingUnknownClient() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", 999).with(merchant()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found with id 999"));
    }

    @Test
    void shouldCreateClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessId": %d,
                                  "firstName": "Marie",
                                  "lastName": "Dupont",
                                  "email": "marie.dupont@example.com",
                                  "phone": "0601020304",
                                  "active": true
                                }
                                """.formatted(business.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName").value("Marie"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.email").value("marie.dupont@example.com"))
                .andExpect(jsonPath("$.phone").value("0601020304"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void shouldRejectInvalidClient() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessId": %d,
                                  "firstName": "",
                                  "lastName": "",
                                  "email": "invalid-email",
                                  "phone": ""
                                }
                                """.formatted(business.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateClient() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .business(business)
                .firstName("Marie")
                .lastName("Dupont")
                .email("marie.dupont@example.com")
                .phone("0601020304")
                .build());

        mockMvc.perform(put("/api/clients/{id}", client.getId())
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessId": %d,
                                  "firstName": "Marie",
                                  "lastName": "Martin",
                                  "email": "marie.martin@example.com",
                                  "phone": "0611223344",
                                  "active": false
                                }
                                """.formatted(business.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.firstName").value("Marie"))
                .andExpect(jsonPath("$.lastName").value("Martin"))
                .andExpect(jsonPath("$.email").value("marie.martin@example.com"))
                .andExpect(jsonPath("$.phone").value("0611223344"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownClient() throws Exception {
        mockMvc.perform(put("/api/clients/{id}", 999)
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessId": %d,
                                  "firstName": "Marie",
                                  "lastName": "Martin",
                                  "email": "marie.martin@example.com",
                                  "phone": "0611223344",
                                  "active": true
                                }
                                """.formatted(business.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found with id 999"));
    }
}
