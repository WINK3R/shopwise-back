package com.example.shopwiseapi.web;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.CustomerAccount;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @BeforeEach
    void setUp() {
        customerAccountRepository.deleteAll();
        loyaltyAccountRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomerAccount() throws Exception {
        Client client = saveClient();

        mockMvc.perform(post("/api/clients/{clientId}/account", client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "passwordHash": "hash-secret",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldFindCustomerAccountByClientId() throws Exception {
        Client client = saveClient();
        customerAccountRepository.save(CustomerAccount.builder()
                .client(client)
                .passwordHash("hash-secret")
                .active(false)
                .build());

        mockMvc.perform(get("/api/clients/{clientId}/account", client.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturnConflictWhenCustomerAccountAlreadyExists() throws Exception {
        Client client = saveClient();
        customerAccountRepository.save(CustomerAccount.builder()
                .client(client)
                .passwordHash("hash-secret")
                .build());

        mockMvc.perform(post("/api/clients/{clientId}/account", client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "passwordHash": "new-hash"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Customer account already exists for client " + client.getId()));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerAccountDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/clients/{clientId}/account", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer account not found for client 999"));
    }

    private Client saveClient() {
        return clientRepository.save(Client.builder()
                .firstName("Marie")
                .lastName("Dupont")
                .email("marie.dupont@example.com")
                .phone("0601020304")
                .build());
    }
}
