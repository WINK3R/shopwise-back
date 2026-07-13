package com.example.shopwiseapi.security;

import com.example.shopwiseapi.merchant.MerchantAccount;
import com.example.shopwiseapi.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantUserDetailsService implements UserDetailsService {

    private final MerchantAccountRepository merchantAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        MerchantAccount account = merchantAccountRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Merchant account not found"));
        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .disabled(!account.getActive())
                .authorities("MERCHANT")
                .build();
    }
}
