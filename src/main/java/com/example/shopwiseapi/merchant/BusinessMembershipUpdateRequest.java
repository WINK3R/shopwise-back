package com.example.shopwiseapi.merchant;

import jakarta.validation.constraints.AssertTrue;

public record BusinessMembershipUpdateRequest(MembershipRole role, Boolean active) {

    @AssertTrue(message = "role or active must be provided")
    public boolean isNotEmpty() {
        return role != null || active != null;
    }
}
