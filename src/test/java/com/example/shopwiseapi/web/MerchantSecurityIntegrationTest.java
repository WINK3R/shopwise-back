package com.example.shopwiseapi.web;

import com.example.shopwiseapi.appointment.Appointment;
import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.catalog.Service;
import com.example.shopwiseapi.client.Client;
import com.example.shopwiseapi.client.LoyaltyAccount;
import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.InvitationStatus;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.merchant.MerchantInvitation;
import com.example.shopwiseapi.repository.AppointmentRepository;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import com.example.shopwiseapi.repository.BusinessRepository;
import com.example.shopwiseapi.repository.CatalogServiceRepository;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.LoyaltyAccountRepository;
import com.example.shopwiseapi.repository.MerchantAccountRepository;
import com.example.shopwiseapi.repository.MerchantInvitationRepository;
import com.example.shopwiseapi.service.BusinessDefaultsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MerchantSecurityIntegrationTest extends AbstractMerchantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MerchantAccountRepository accountRepository;

    @Autowired
    private BusinessMembershipRepository membershipRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private MerchantInvitationRepository invitationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CatalogServiceRepository serviceRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BusinessDefaultsService businessDefaultsService;

    @Test
    void shouldLoginRestoreSessionAndLogout() throws Exception {
        mockMvc.perform(get("/api/merchant-auth/session"))
                .andExpect(status().isUnauthorized());

        MvcResult loginResult = mockMvc.perform(post("/api/merchant-auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"owner@shopwise.test","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.email").value("owner@shopwise.test"))
                .andExpect(jsonPath("$.account.lastLogin").isNotEmpty())
                .andExpect(jsonPath("$.businesses[0].businessId").value(business.getId()))
                .andExpect(jsonPath("$.businesses[0].role").value("OWNER"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        mockMvc.perform(get("/api/merchant-auth/session").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.id").value(merchantAccount.getId()));

        mockMvc.perform(post("/api/merchant-auth/logout").session(session).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/merchant-auth/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidMerchantCredentials() throws Exception {
        mockMvc.perform(post("/api/merchant-auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"owner@shopwise.test","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid merchant credentials"));
    }

    @Test
    void shouldCreateSeveralBusinessesAndAttachCreatorAsOwner() throws Exception {
        mockMvc.perform(post("/api/businesses")
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Deuxieme boutique",
                                  "email":"contact@deuxieme.fr",
                                  "phone":"0101010101"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Deuxieme boutique"));

        mockMvc.perform(get("/api/me/businesses").with(merchant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        Business secondBusiness = businessRepository.findAll().stream()
                .filter(value -> value.getName().equals("Deuxieme boutique"))
                .findFirst()
                .orElseThrow();
        BusinessMembership membership = membershipRepository
                .findByMerchantAccountIdAndBusinessId(merchantAccount.getId(), secondBusiness.getId())
                .orElseThrow();
        assertThat(membership.getRole()).isEqualTo(MembershipRole.OWNER);
        assertThat(serviceRepository.findByBusinessIdOrderByName(secondBusiness.getId()))
                .hasSize(3)
                .extracting(Service::getName, Service::getDurationMinutes, Service::getLoyaltyPoints)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Conseil personnalis\u00e9", 45, 25),
                        org.assertj.core.groups.Tuple.tuple("Retrait de commande", 30, 10),
                        org.assertj.core.groups.Tuple.tuple("Atelier d\u00e9couverte", 60, 40)
                );
    }

    @Test
    void shouldCreateDefaultServicesOnlyOncePerBusiness() {
        businessDefaultsService.createDefaultServices(business);
        businessDefaultsService.createDefaultServices(business);

        assertThat(serviceRepository.findByBusinessIdOrderByName(business.getId())).hasSize(3);
    }

    @Test
    void shouldIsolateClientsByBusinessAndReturnForbiddenWithoutMembership() throws Exception {
        Business secondBusiness = saveBusiness("Autre commerce", "autre@commerce.fr");
        saveClient(business, "one@client.fr");
        saveClient(secondBusiness, "two@client.fr");

        mockMvc.perform(get("/api/clients")
                        .param("businessId", secondBusiness.getId().toString())
                        .with(merchant()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Business access denied"));

        membershipRepository.save(BusinessMembership.builder()
                .merchantAccount(merchantAccount)
                .business(secondBusiness)
                .role(MembershipRole.OWNER)
                .build());

        mockMvc.perform(get("/api/clients")
                        .param("businessId", business.getId().toString())
                        .with(merchant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("one@client.fr"));
        mockMvc.perform(get("/api/clients")
                        .param("businessId", secondBusiness.getId().toString())
                        .with(merchant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("two@client.fr"));
    }

    @Test
    void shouldEnforceStaffPermissionsWhileAllowingAppointments() throws Exception {
        MerchantAccount staff = saveAccount("staff@shopwise.test");
        membershipRepository.save(BusinessMembership.builder()
                .merchantAccount(staff)
                .business(business)
                .role(MembershipRole.STAFF)
                .build());
        Client client = saveClient(business, "staff-client@example.com");
        Service service = saveService(business);
        loyaltyAccountRepository.save(LoyaltyAccount.builder().client(client).pointsBalance(10).build());

        mockMvc.perform(post("/api/services")
                        .with(user(staff.getEmail())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceJson(business.getId())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/clients/{id}/loyalty/credit", client.getId())
                        .with(user(staff.getEmail())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"points\":5}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/appointments")
                        .with(user(staff.getEmail())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessId":%d,
                                  "clientId":%d,
                                  "serviceId":%d,
                                  "startsAt":"2026-07-20T09:30:00"
                                }
                                """.formatted(business.getId(), client.getId(), service.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdByAccountId").value(staff.getId()));
        Appointment appointment = appointmentRepository.findAll().getFirst();
        assertThat(appointment.getBusiness().getId()).isEqualTo(business.getId());
    }

    @Test
    void shouldAllowManagerBusinessDataButDenyTeamManagement() throws Exception {
        MerchantAccount manager = saveAccount("manager@shopwise.test");
        membershipRepository.save(BusinessMembership.builder()
                .merchantAccount(manager)
                .business(business)
                .role(MembershipRole.MANAGER)
                .build());

        mockMvc.perform(post("/api/services")
                        .with(user(manager.getEmail())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceJson(business.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/businesses/{id}/members", business.getId())
                        .with(user(manager.getEmail())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/businesses")
                        .with(user(manager.getEmail())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Commerce interdit",
                                  "email":"forbidden@commerce.fr",
                                  "phone":"0101010101"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldInviteAndRegisterNewMerchantOnlyOnce() throws Exception {
        mockMvc.perform(post("/api/businesses/{id}/invitations", business.getId())
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@shopwise.test\",\"role\":\"STAFF\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.token").isNotEmpty());
        MerchantInvitation invitation = invitationRepository.findAll().getFirst();

        mockMvc.perform(post("/api/merchant-invitations/{token}/accept", invitation.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Nina",
                                  "lastName":"Martin",
                                  "password":"new-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.email").value("new@shopwise.test"))
                .andExpect(jsonPath("$.businesses[0].role").value("STAFF"));

        MerchantAccount created = accountRepository.findByEmailIgnoreCase("new@shopwise.test").orElseThrow();
        assertThat(passwordEncoder.matches("new-password", created.getPasswordHash())).isTrue();

        mockMvc.perform(post("/api/merchant-invitations/{token}/accept", invitation.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invitation has already been processed"));
    }

    @Test
    void shouldRequireExistingAccountLoginBeforeInvitationAcceptance() throws Exception {
        MerchantAccount existing = saveAccount("existing@shopwise.test");
        MerchantInvitation invitation = saveInvitation(existing.getEmail(), LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/api/merchant-invitations/{token}/accept", invitation.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Login required before accepting this invitation"));

        mockMvc.perform(post("/api/merchant-invitations/{token}/accept", invitation.getToken())
                        .with(user(existing.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businesses[0].businessId").value(business.getId()));
    }

    @Test
    void shouldExpireResendAndRevokeInvitations() throws Exception {
        MerchantInvitation expired = saveInvitation(
                "expired@shopwise.test",
                LocalDateTime.now().minusMinutes(1)
        );
        mockMvc.perform(post("/api/merchant-invitations/{token}/accept", expired.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ex","lastName":"Pired","password":"password123"}
                                """))
                .andExpect(status().isGone());
        assertThat(invitationRepository.findById(expired.getId()).orElseThrow().getStatus())
                .isEqualTo(InvitationStatus.EXPIRED);

        String previousToken = expired.getToken();
        mockMvc.perform(post("/api/merchant-invitations/{id}/resend", expired.getId())
                        .with(merchant()).with(csrfToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
        MerchantInvitation resent = invitationRepository.findById(expired.getId()).orElseThrow();
        assertThat(resent.getToken()).isNotEqualTo(previousToken);

        mockMvc.perform(post("/api/merchant-invitations/{id}/revoke", expired.getId())
                        .with(merchant()).with(csrfToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
    }

    @Test
    void shouldPreventRemovingLastOwner() throws Exception {
        BusinessMembership ownerMembership = membershipRepository
                .findByMerchantAccountIdAndBusinessId(merchantAccount.getId(), business.getId())
                .orElseThrow();
        mockMvc.perform(patch(
                        "/api/businesses/{businessId}/members/{membershipId}",
                        business.getId(),
                        ownerMembership.getId()
                )
                        .with(merchant()).with(csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A business must keep at least one active owner"));
    }

    private MerchantAccount saveAccount(String email) {
        return accountRepository.save(MerchantAccount.builder()
                .firstName("Test")
                .lastName("Merchant")
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .build());
    }

    private Business saveBusiness(String name, String email) {
        return businessRepository.save(Business.builder()
                .name(name)
                .email(email)
                .phone("0102030405")
                .build());
    }

    private Client saveClient(Business targetBusiness, String email) {
        return clientRepository.save(Client.builder()
                .business(targetBusiness)
                .firstName("Client")
                .lastName("Test")
                .email(email)
                .phone("0601020304")
                .build());
    }

    private Service saveService(Business targetBusiness) {
        return serviceRepository.save(Service.builder()
                .business(targetBusiness)
                .name("Consultation")
                .description("Consultation test")
                .durationMinutes(30)
                .loyaltyPoints(10)
                .build());
    }

    private MerchantInvitation saveInvitation(String email, LocalDateTime expiresAt) {
        return invitationRepository.save(MerchantInvitation.builder()
                .business(business)
                .email(email)
                .role(MembershipRole.STAFF)
                .token(java.util.UUID.randomUUID().toString())
                .expiresAt(expiresAt)
                .createdBy(merchantAccount)
                .build());
    }

    private String serviceJson(Long businessId) {
        return """
                {
                  "businessId":%d,
                  "name":"Forbidden service",
                  "description":"Test",
                  "durationMinutes":30,
                  "loyaltyPoints":10
                }
                """.formatted(businessId);
    }
}
