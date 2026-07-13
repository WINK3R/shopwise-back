package com.example.shopwiseapi.web;

import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.merchant.MerchantInvitationAcceptRequest;
import com.example.shopwiseapi.merchant.MerchantInvitationResponse;
import com.example.shopwiseapi.merchant.MerchantSessionResponse;
import com.example.shopwiseapi.security.MerchantUserDetailsService;
import com.example.shopwiseapi.service.MerchantAuthService;
import com.example.shopwiseapi.service.MerchantInvitationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant-invitations")
@RequiredArgsConstructor
public class MerchantInvitationController {

    private final MerchantInvitationService invitationService;
    private final MerchantAuthService merchantAuthService;
    private final MerchantUserDetailsService userDetailsService;
    private final MerchantAuthController merchantAuthController;

    @PostMapping("/{token}/accept")
    public MerchantSessionResponse accept(
            @PathVariable String token,
            @RequestBody MerchantInvitationAcceptRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            CsrfToken csrfToken
    ) {
        MerchantAccount account = invitationService.accept(token, request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(account.getEmail());
        merchantAuthController.establishSession(
                UsernamePasswordAuthenticationToken.authenticated(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                ),
                httpRequest,
                httpResponse
        );
        csrfToken.getToken();
        return merchantAuthService.toSession(account);
    }

    @PostMapping("/{id}/resend")
    public MerchantInvitationResponse resend(@PathVariable Long id) {
        return invitationService.resend(id);
    }

    @PostMapping("/{id}/revoke")
    public MerchantInvitationResponse revoke(@PathVariable Long id) {
        return invitationService.revoke(id);
    }
}
