package com.example.shopwiseapi.business;

public record BusinessResponse(Long id, String name, String email, String phone, Boolean active) {

    public static BusinessResponse from(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getName(),
                business.getEmail(),
                business.getPhone(),
                business.getActive()
        );
    }
}
