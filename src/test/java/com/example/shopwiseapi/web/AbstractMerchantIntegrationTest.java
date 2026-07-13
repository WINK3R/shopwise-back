package com.example.shopwiseapi.web;

import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.repository.AppointmentRepository;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import com.example.shopwiseapi.repository.BusinessRepository;
import com.example.shopwiseapi.repository.CatalogServiceRepository;
import com.example.shopwiseapi.repository.ClientRepository;
import com.example.shopwiseapi.repository.CustomerAccountRepository;
import com.example.shopwiseapi.repository.LoyaltyAccountRepository;
import com.example.shopwiseapi.repository.LoyaltyTransactionRepository;
import com.example.shopwiseapi.repository.MerchantAccountRepository;
import com.example.shopwiseapi.repository.MerchantInvitationRepository;
import com.example.shopwiseapi.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

abstract class AbstractMerchantIntegrationTest {

    @Autowired
    private LoyaltyTransactionRepository baseTransactionRepository;

    @Autowired
    private AppointmentRepository baseAppointmentRepository;

    @Autowired
    private CustomerAccountRepository baseCustomerAccountRepository;

    @Autowired
    private LoyaltyAccountRepository baseLoyaltyAccountRepository;

    @Autowired
    private MerchantInvitationRepository baseInvitationRepository;

    @Autowired
    private BusinessMembershipRepository baseMembershipRepository;

    @Autowired
    private MerchantRepository baseMerchantRepository;

    @Autowired
    private CatalogServiceRepository baseServiceRepository;

    @Autowired
    private ClientRepository baseClientRepository;

    @Autowired
    private BusinessRepository baseBusinessRepository;

    @Autowired
    private MerchantAccountRepository baseAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected Business business;
    protected MerchantAccount merchantAccount;

    @BeforeEach
    void setUpMerchantContext() {
        clearDatabase();
        business = baseBusinessRepository.save(Business.builder()
                .name("Chez Marie")
                .email("contact@chez-marie.fr")
                .phone("0102030405")
                .build());
        merchantAccount = baseAccountRepository.save(MerchantAccount.builder()
                .firstName("Marie")
                .lastName("Dupont")
                .email("owner@shopwise.test")
                .passwordHash(passwordEncoder.encode("password123"))
                .build());
        baseMembershipRepository.save(BusinessMembership.builder()
                .business(business)
                .merchantAccount(merchantAccount)
                .role(MembershipRole.OWNER)
                .build());
    }

    protected RequestPostProcessor merchant() {
        return user(merchantAccount.getEmail()).authorities(() -> "MERCHANT");
    }

    protected RequestPostProcessor csrfToken() {
        return csrf();
    }

    protected void clearDatabase() {
        baseTransactionRepository.deleteAll();
        baseAppointmentRepository.deleteAll();
        baseCustomerAccountRepository.deleteAll();
        baseLoyaltyAccountRepository.deleteAll();
        baseInvitationRepository.deleteAll();
        baseMembershipRepository.deleteAll();
        baseMerchantRepository.deleteAll();
        baseServiceRepository.deleteAll();
        baseClientRepository.deleteAll();
        baseBusinessRepository.deleteAll();
        baseAccountRepository.deleteAll();
    }
}
