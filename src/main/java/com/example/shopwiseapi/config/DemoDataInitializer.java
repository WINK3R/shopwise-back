package com.example.shopwiseapi.config;

import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import com.example.shopwiseapi.repository.BusinessRepository;
import com.example.shopwiseapi.repository.MerchantAccountRepository;
import com.example.shopwiseapi.service.BusinessDefaultsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "shopwise.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements ApplicationRunner {

    private final MerchantAccountRepository accountRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMembershipRepository membershipRepository;
    private final BusinessDefaultsService businessDefaultsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${shopwise.bootstrap.email:owner@shopwise.local}")
    private String email;

    @Value("${shopwise.bootstrap.password:Shopwise123!}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        MerchantAccount owner = accountRepository.findByEmailIgnoreCase(email.trim())
                .orElseGet(() -> accountRepository.save(MerchantAccount.builder()
                        .firstName("Marie")
                        .lastName("Dupont")
                        .email(email.trim().toLowerCase())
                        .passwordHash(passwordEncoder.encode(password))
                        .build()));
        Business business = businessRepository.findByEmailIgnoreCase("contact@chez-marie.local")
                .orElseGet(() -> businessRepository.save(Business.builder()
                        .name("Chez Marie")
                        .email("contact@chez-marie.local")
                        .phone("0102030405")
                        .build()));
        if (!membershipRepository.existsByMerchantAccountIdAndBusinessId(owner.getId(), business.getId())) {
            membershipRepository.save(BusinessMembership.builder()
                    .merchantAccount(owner)
                    .business(business)
                    .role(MembershipRole.OWNER)
                    .build());
        }

        businessDefaultsService.createDefaultServices(business);
        log.info("Demo data initialized for {}. Disable bootstrap in production.", business.getName());
    }
}
