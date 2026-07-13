package com.example.shopwiseapi.service;

import com.example.shopwiseapi.business.Business;
import com.example.shopwiseapi.merchant.BusinessMembership;
import com.example.shopwiseapi.merchant.InvitationStatus;
import com.example.shopwiseapi.merchant.MembershipRole;
import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.merchant.MerchantInvitation;
import com.example.shopwiseapi.merchant.MerchantInvitationAcceptRequest;
import com.example.shopwiseapi.merchant.MerchantInvitationRequest;
import com.example.shopwiseapi.merchant.MerchantInvitationResponse;
import com.example.shopwiseapi.repository.BusinessMembershipRepository;
import com.example.shopwiseapi.repository.MerchantAccountRepository;
import com.example.shopwiseapi.repository.MerchantInvitationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantInvitationService {

    private final MerchantInvitationRepository invitationRepository;
    private final MerchantAccountRepository accountRepository;
    private final BusinessMembershipRepository membershipRepository;
    private final BusinessAccessService businessAccessService;
    private final BusinessService businessService;
    private final PasswordEncoder passwordEncoder;

    @Value("${shopwise.invitation.expiration-hours:48}")
    private long expirationHours;

    @Value("${shopwise.invitation.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public MerchantInvitationResponse create(Long businessId, MerchantInvitationRequest request) {
        MerchantAccount creator = businessAccessService
                .requireMembership(businessId, MembershipRole.OWNER)
                .getMerchantAccount();
        Business business = businessService.findEntity(businessId);
        String email = normalizeEmail(request.email());

        accountRepository.findByEmailIgnoreCase(email).ifPresent(account -> {
            if (membershipRepository.existsByMerchantAccountIdAndBusinessId(account.getId(), businessId)) {
                throw new ResourceAlreadyExistsException("Account is already a member of business " + businessId);
            }
        });
        if (invitationRepository.existsByBusinessIdAndEmailIgnoreCaseAndStatus(
                businessId,
                email,
                InvitationStatus.PENDING
        )) {
            throw new ResourceAlreadyExistsException("A pending invitation already exists for " + email);
        }

        MerchantInvitation invitation = MerchantInvitation.builder()
                .business(business)
                .email(email)
                .role(request.role())
                .token(newToken())
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .createdBy(creator)
                .build();
        MerchantInvitation savedInvitation = invitationRepository.save(invitation);
        logInvitationLink(savedInvitation);
        return MerchantInvitationResponse.from(savedInvitation);
    }

    @Transactional
    public List<MerchantInvitationResponse> findByBusiness(Long businessId) {
        businessAccessService.requireMembership(businessId, MembershipRole.OWNER);
        LocalDateTime now = LocalDateTime.now();
        return invitationRepository.findByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream()
                .peek(invitation -> markExpired(invitation, now))
                .map(MerchantInvitationResponse::from)
                .toList();
    }

    @Transactional
    public MerchantInvitationResponse resend(Long invitationId) {
        MerchantInvitation invitation = findById(invitationId);
        businessAccessService.requireMembership(invitation.getBusiness().getId(), MembershipRole.OWNER);
        if (invitation.getStatus() == InvitationStatus.ACCEPTED
                || invitation.getStatus() == InvitationStatus.REVOKED) {
            throw new InvalidOperationException("Invitation cannot be resent with status " + invitation.getStatus());
        }
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setToken(newToken());
        invitation.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        logInvitationLink(invitation);
        return MerchantInvitationResponse.from(invitation);
    }

    @Transactional
    public MerchantInvitationResponse revoke(Long invitationId) {
        MerchantInvitation invitation = findById(invitationId);
        businessAccessService.requireMembership(invitation.getBusiness().getId(), MembershipRole.OWNER);
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidOperationException("Only a pending invitation can be revoked");
        }
        invitation.setStatus(InvitationStatus.REVOKED);
        return MerchantInvitationResponse.from(invitation);
    }

    @Transactional(noRollbackFor = InvitationExpiredException.class)
    public MerchantAccount accept(String token, MerchantInvitationAcceptRequest request) {
        MerchantInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidOperationException("Invitation has already been processed");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            throw new InvitationExpiredException("Invitation has expired");
        }

        MerchantAccount account = accountRepository.findByEmailIgnoreCase(invitation.getEmail())
                .map(this::requireAuthenticatedAccount)
                .orElseGet(() -> createAccount(invitation.getEmail(), request));
        if (membershipRepository.existsByMerchantAccountIdAndBusinessId(
                account.getId(),
                invitation.getBusiness().getId()
        )) {
            throw new ResourceAlreadyExistsException("Account is already a member of this business");
        }

        membershipRepository.save(BusinessMembership.builder()
                .merchantAccount(account)
                .business(invitation.getBusiness())
                .role(invitation.getRole())
                .build());
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        return account;
    }

    private MerchantAccount requireAuthenticatedAccount(MerchantAccount account) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !account.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new AuthenticationFailedException("Login required before accepting this invitation");
        }
        return account;
    }

    private MerchantAccount createAccount(String email, MerchantInvitationAcceptRequest request) {
        if (!StringUtils.hasText(request.firstName()) || !StringUtils.hasText(request.lastName())
                || !StringUtils.hasText(request.password()) || request.password().length() < 8) {
            throw new InvalidOperationException(
                    "firstName, lastName and a password of at least 8 characters are required"
            );
        }
        return accountRepository.save(MerchantAccount.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build());
    }

    private MerchantInvitation findById(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found with id " + invitationId));
    }

    private void markExpired(MerchantInvitation invitation, LocalDateTime now) {
        if (invitation.getStatus() == InvitationStatus.PENDING && invitation.getExpiresAt().isBefore(now)) {
            invitation.setStatus(InvitationStatus.EXPIRED);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String newToken() {
        return UUID.randomUUID().toString();
    }

    private void logInvitationLink(MerchantInvitation invitation) {
        log.info(
                "Merchant invitation for {}: {}/invitation-commercant/{}",
                invitation.getEmail(),
                frontendUrl,
                invitation.getToken()
        );
    }
}
