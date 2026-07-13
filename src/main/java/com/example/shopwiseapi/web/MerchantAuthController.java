package com.example.shopwiseapi.web;

import com.example.shopwiseapi.merchant.BusinessMembershipResponse;
import com.example.shopwiseapi.merchant.MerchantLoginRequest;
import com.example.shopwiseapi.merchant.MerchantSessionResponse;
import com.example.shopwiseapi.service.MerchantAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MerchantAuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final MerchantAuthService merchantAuthService;

    @PostMapping("/api/merchant-auth/login")
    public MerchantSessionResponse login(
            @Valid @RequestBody MerchantLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            CsrfToken csrfToken
    ) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
        );
        establishSession(authentication, httpRequest, httpResponse);
        csrfToken.getToken();
        return merchantAuthService.recordLoginAndGetSession(authentication.getName());
    }

    @PostMapping("/api/merchant-auth/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(emptyContext);
        securityContextRepository.saveContext(emptyContext, request, response);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/api/merchant-auth/session")
    public MerchantSessionResponse session(CsrfToken csrfToken) {
        csrfToken.getToken();
        return merchantAuthService.getCurrentSession();
    }

    @GetMapping("/api/me/businesses")
    public List<BusinessMembershipResponse> businesses() {
        return merchantAuthService.getCurrentBusinesses();
    }

    public void establishSession(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
