package com.example.shopwiseapi.merchant;

import java.util.List;

public record MerchantSessionResponse(
        MerchantAccountResponse account,
        List<BusinessMembershipResponse> businesses
) {
}
