package com.example.shopwiseapi.web;

import com.example.shopwiseapi.appointment.Appointment;
import com.example.shopwiseapi.appointment.AppointmentStatus;
import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.catalog.Service;
import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.repository.AppointmentRepository;
import com.example.shopwiseapi.repository.BusinessRepository;
import com.example.shopwiseapi.repository.CatalogServiceRepository;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.CustomerAccountRepository;
import com.example.shopwiseapi.repository.LoyaltyAccountRepository;
import com.example.shopwiseapi.repository.LoyaltyTransactionRepository;
import com.example.shopwiseapi.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerTest extends AbstractMerchantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CatalogServiceRepository serviceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Test
    void shouldCreateAppointmentAndComputeEndTime() throws Exception {
        Fixtures fixtures = saveFixtures();

        mockMvc.perform(post("/api/appointments")
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(fixtures, "2026-07-20T09:30:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.businessId").value(fixtures.business().getId()))
                .andExpect(jsonPath("$.clientId").value(fixtures.client().getId()))
                .andExpect(jsonPath("$.serviceId").value(fixtures.service().getId()))
                .andExpect(jsonPath("$.createdByAccountId").value(merchantAccount.getId()))
                .andExpect(jsonPath("$.startsAt").value("2026-07-20T09:30:00"))
                .andExpect(jsonPath("$.endsAt").value("2026-07-20T10:15:00"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldFilterAppointmentsByDateStatusAndClient() throws Exception {
        Fixtures fixtures = saveFixtures();
        saveAppointment(fixtures, LocalDateTime.of(2026, 7, 20, 9, 30), AppointmentStatus.SCHEDULED);
        saveAppointment(fixtures, LocalDateTime.of(2026, 7, 21, 9, 30), AppointmentStatus.CANCELED);

        mockMvc.perform(get("/api/appointments")
                        .with(merchant())
                        .param("businessId", business.getId().toString())
                        .param("date", "2026-07-20")
                        .param("status", "SCHEDULED")
                        .param("clientId", fixtures.client().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].startsAt").value("2026-07-20T09:30:00"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void shouldCreditPointsOnceWhenAppointmentIsHonored() throws Exception {
        Fixtures fixtures = saveFixtures();
        Appointment appointment = saveAppointment(
                fixtures,
                LocalDateTime.of(2026, 7, 20, 9, 30),
                AppointmentStatus.SCHEDULED
        );

        String statusBody = """
                {"status":"HONORED"}
                """;
        mockMvc.perform(patch("/api/appointments/{id}/status", appointment.getId())
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HONORED"));

        mockMvc.perform(patch("/api/appointments/{id}/status", appointment.getId())
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clients/{clientId}/loyalty", fixtures.client().getId()).with(merchant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance").value(25));

        mockMvc.perform(get(
                        "/api/clients/{clientId}/loyalty/transactions",
                        fixtures.client().getId()
                ).with(merchant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].appointmentId").value(appointment.getId()))
                .andExpect(jsonPath("$[0].type").value("EARNED"))
                .andExpect(jsonPath("$[0].pointsDelta").value(25));
    }

    @Test
    void shouldCancelAppointmentAndRejectLaterCompletion() throws Exception {
        Fixtures fixtures = saveFixtures();
        Appointment appointment = saveAppointment(
                fixtures,
                LocalDateTime.of(2026, 7, 20, 9, 30),
                AppointmentStatus.SCHEDULED
        );

        mockMvc.perform(patch("/api/appointments/{id}/status", appointment.getId())
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CANCELED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(patch("/api/appointments/{id}/status", appointment.getId())
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"HONORED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Appointment status cannot change from CANCELED to HONORED"
                ));
    }

    private Fixtures saveFixtures() {
        Client client = clientRepository.save(Client.builder()
                .business(business)
                .firstName("Alice")
                .lastName("Martin")
                .email("alice@example.com")
                .phone("0601020304")
                .build());
        Service service = serviceRepository.save(Service.builder()
                .business(business)
                .name("Consultation")
                .description("Conseil personnalise")
                .durationMinutes(45)
                .loyaltyPoints(25)
                .build());
        return new Fixtures(business, client, service);
    }

    private Appointment saveAppointment(
            Fixtures fixtures,
            LocalDateTime startsAt,
            AppointmentStatus status
    ) {
        return appointmentRepository.save(Appointment.builder()
                .business(fixtures.business())
                .client(fixtures.client())
                .service(fixtures.service())
                .createdBy(merchantAccount)
                .startsAt(startsAt)
                .endsAt(startsAt.plusMinutes(fixtures.service().getDurationMinutes()))
                .status(status)
                .build());
    }

    private String appointmentJson(Fixtures fixtures, String startsAt) {
        return """
                {
                  "businessId": %d,
                  "clientId": %d,
                  "serviceId": %d,
                  "startsAt": "%s",
                  "comment": "Premier rendez-vous"
                }
                """.formatted(
                fixtures.business().getId(),
                fixtures.client().getId(),
                fixtures.service().getId(),
                startsAt
        );
    }

    private record Fixtures(Business business, Client client, Service service) {
    }
}
