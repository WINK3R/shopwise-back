package com.example.shopwiseapi.catalog;

public record ServiceResponse(
        Long id,
        Long businessId,
        String name,
        String description,
        Integer durationMinutes,
        Integer loyaltyPoints,
        Boolean active
) {
    public static ServiceResponse from(Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getBusiness().getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getLoyaltyPoints(),
                service.getActive()
        );
    }
}
