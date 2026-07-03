package com.example.shopwiseapi.web;

import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.LoyaltyAccount;
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
class LoyaltyAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @BeforeEach
    void setUp() {
        customerAccountRepository.deleteAll();
        loyaltyAccountRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldCreateLoyaltyAccount() throws Exception {
        Client client = saveClient();

        mockMvc.perform(post("/api/clients/{clientId}/loyalty", client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pointsBalance": 20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.pointsBalance").value(20))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void shouldFindLoyaltyAccountByClientId() throws Exception {
        Client client = saveClient();
        loyaltyAccountRepository.save(LoyaltyAccount.builder()
                .client(client)
                .pointsBalance(10)
                .build());

        mockMvc.perform(get("/api/clients/{clientId}/loyalty", client.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.pointsBalance").value(10))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void shouldCreditLoyaltyAccount() throws Exception {
        Client client = saveClient();
        loyaltyAccountRepository.save(LoyaltyAccount.builder()
                .client(client)
                .pointsBalance(10)
                .build());

        mockMvc.perform(post("/api/clients/{clientId}/loyalty/credit", client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "points": 15
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(25));
    }

    @Test
    void shouldDebitLoyaltyAccount() throws Exception {
        Client client = saveClient();
        loyaltyAccountRepository.save(LoyaltyAccount.builder()
                .client(client)
                .pointsBalance(10)
                .build());

        mockMvc.perform(post("/api/clients/{clientId}/loyalty/debit", client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "points": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(6));
    }

    @Test
    void shouldRejectInvalidPoints() throws Exception {
        Client client = saveClient();
        loyaltyAccountRepository.save(LoyaltyAccount.builder()
                .client(client)
                .pointsBalance(10)
                .build());

        mockMvc.perform(post("/api/clients/{clientId}/loyalty/credit", client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "points": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
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
