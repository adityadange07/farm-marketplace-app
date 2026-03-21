package com.farmmarket.mapper;

import com.farmmarket.dto.response.FarmResponse;
import com.farmmarket.entity.Farm;
import org.springframework.stereotype.Component;

@Component
public class FarmMapper {

    public FarmResponse toResponse(Farm farm) {
        if (farm == null) return null;

        return FarmResponse.builder()
                .id(farm.getId())
                .farmerId(farm.getFarmer().getId())
                .farmName(farm.getFarmName())
                .description(farm.getDescription())
                .addressLine1(farm.getAddressLine1())
                .city(farm.getCity())
                .state(farm.getState())
                .zipCode(farm.getZipCode())
                .latitude(farm.getLatitude().doubleValue())
                .longitude(farm.getLongitude().doubleValue())
                .isOrganic(farm.getIsOrganic())
                .verificationStatus(farm.getVerificationStatus().name())
                .deliveryRadiusKm(farm.getDeliveryRadiusKm())
                .minimumOrder(farm.getMinimumOrder())
                .rating(farm.getRating())
                .totalReviews(farm.getTotalReviews())
                .bannerImage(farm.getBannerImage())
                .certifications(farm.getCertifications())
                .createdAt(farm.getCreatedAt())
                .build();
    }
}